package dev.alexnader.framity.data.overlay.json

import com.google.gson.*
import java.lang.reflect.Type

fun GsonBuilder.registerOverlayHandlers() = this.apply {
    registerTypeAdapter(TextureSource::class.java, TextureSource.Deserializer())
    registerTypeAdapter(TextureSource.Single::class.java, TextureSource.Single.Deserializer())
    registerTypeAdapter(TextureSource.Sided::class.java, TextureSource.Sided.Deserializer())
    registerTypeAdapter(TextureOffsets::class.java, TextureOffsets.Deserializer())
    registerTypeAdapter(Offsetter::class.java, Offsetter.Deserializer())
    registerTypeAdapter(Offsetter.Remap::class.java, Offsetter.Remap.Deserializer())
}

data class OverlayDefinition(val item: String, val overlay: OverlayInfo)

data class OverlayInfo(val textureSource: TextureSource, val coloredLike: String? = null, val textureOffsets: TextureOffsets? = null)

sealed class TextureSource {
    class Deserializer : JsonDeserializer<TextureSource> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): TextureSource =
            json.asJsonObject.let { obj ->
                when {
                    obj.has("single") -> context.deserialize<Single>(obj["single"], Single::class.java)
                    obj.has("sided") -> context.deserialize<Sided>(obj["sided"], Sided::class.java)
                    else -> throw JsonParseException("Invalid Texture Source: $obj")
                }
            }
    }

    data class Single(val sprite: String) : TextureSource() {
        class Deserializer : JsonDeserializer<Single> {
            override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Single =
                Single(context.deserialize<String>(json, String::class.java))
        }
    }
    data class Sided(val elements: Array<Element>) : TextureSource() {
        class Deserializer : JsonDeserializer<Sided> {
            override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Sided =
                Sided(context.deserialize<Array<Element>>(json, Array<Element>::class.java))
        }

        data class Element(val sides: List<String>, val value: String)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Sided

            if (!elements.contentEquals(other.elements)) return false

            return true
        }
        override fun hashCode(): Int {
            return elements.contentHashCode()
        }
    }
}

data class TextureOffsets(val elements: Array<Element>) {
    class Deserializer : JsonDeserializer<TextureOffsets> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): TextureOffsets =
            TextureOffsets(context.deserialize<Array<Element>>(json, Array<Element>::class.java))
    }

    data class Element(val sides: List<String>, val value: Offsetters)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TextureOffsets

        if (!elements.contentEquals(other.elements)) return false

        return true
    }
    override fun hashCode(): Int {
        return elements.contentHashCode()
    }
}

data class Offsetters(val uOffsetter: Offsetter?, val vOffsetter: Offsetter?)

sealed class Offsetter {
    class Deserializer : JsonDeserializer<Offsetter> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Offsetter =
            json.asJsonObject.let { obj ->
                when {
                    obj.has("remap") -> context.deserialize<Remap>(obj["remap"], Remap::class.java)
                    else -> throw JsonParseException("Invalid Offsetter: $obj")
                }
            }
    }

    data class Remap(val elements: Array<Element>) : Offsetter() {
        class Deserializer : JsonDeserializer<Remap> {
            override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Remap =
                Remap(context.deserialize<Array<Element>>(json, Array<Element>::class.java))
        }

        data class Element(val from: List<Float>, val to: List<Float>)
    }
}
