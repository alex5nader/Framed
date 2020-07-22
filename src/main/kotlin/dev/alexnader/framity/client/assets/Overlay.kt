package dev.alexnader.framity.client.assets

import com.google.gson.JsonElement
import dev.alexnader.framity.GSON
import dev.alexnader.framity.LOGGER
import dev.alexnader.framity.MOD
import dev.alexnader.framity.util.*
import dev.alexnader.framity.util.json.*
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener
import net.minecraft.block.BlockState
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import net.minecraft.util.profiler.Profiler
import net.minecraft.util.registry.Registry
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.function.Supplier
import kotlin.math.max
import kotlin.math.min

private val OverlayInfoMap: MutableMap<Identifier, OverlayInfo> = mutableMapOf()

fun getOverlay(id: Identifier?) =
    id?.let { OverlayInfoMap[it] }
fun getValidOverlay(id: Identifier?) =
    id?.let { OverlayInfoMap[it] as? OverlayInfo.Complete? }

object OverlayAssetsListener : SimpleResourceReloadListener<Collection<Identifier>> {
    private val id = MOD.id("listener/assets/overlay")
    override fun getFabricId() = id

    private fun loadOverlay(manager: ResourceManager, rootOverlayId: Identifier) {
        val dependencies = mutableSetOf<Identifier>()

        fun loadOverlayRec(overlayId: Identifier) {
            val ctx = GSON.fromJson(manager.getResource(overlayId).inputStream.bufferedReader(), JsonElement::class.java).toContext(overlayId.toString())

            ctx.runParser(OverlayInfo.DependenciesParser).forEach {
                if (!dependencies.add(it)) {
                    ctx.error("Circular dependency: $it and $overlayId")
                }
                loadOverlayRec(it)
            }

            OverlayInfoMap[overlayId] = ctx.runParser(OverlayInfo.Parser)
        }

        if (rootOverlayId !in OverlayInfoMap) {
            loadOverlayRec(rootOverlayId)
        }
    }

    override fun load(
        manager: ResourceManager,
        profiler: Profiler,
        executor: Executor
    ): CompletableFuture<Collection<Identifier>> = CompletableFuture.supplyAsync(Supplier {
        manager.findResources("framity/overlays") { s -> s.endsWith(".json") }
    }, executor)

    override fun apply(
        data: Collection<Identifier>,
        manager: ResourceManager,
        profiler: Profiler,
        executor: Executor
    ): CompletableFuture<Void> = CompletableFuture.runAsync(Runnable {
        data.forEach { overlayId ->
            try {
                loadOverlay(manager, overlayId)
            } catch (e: IOException) {
                LOGGER.error("Error while loading a Framity overlay: $e")
            } catch (e: JsonParseException) {
                LOGGER.error("Error while parsing a Framity overlay: $e")
            }
        }
    }, executor)
}

sealed class OverlayInfo {
    object DependenciesParser : JsonParser<List<Identifier>> {
        override fun invoke(ctx: JsonParseContext): List<Identifier> {
            val parent = ctx.getOrNull("parent")?.runParser(IdentifierParser)

            return parent?.let { listOf(it) } ?: listOf()
        }
    }

    object Parser : JsonParser<OverlayInfo> {
        override fun invoke(ctx: JsonParseContext): OverlayInfo {
            val parent = ctx.getOrNull("parent")?.runParser(IdentifierParser)
            val parentInfo = parent?.let { getOverlay(it) }

            val textureSource = ctx.getOrNull("textureSource")?.runParser(TextureSource.Parser)
                ?: parentInfo?.textureSource
            val coloredLike = ctx.getOrNull("coloredLike")?.runParser(ColoredLike.Parser)
                ?: parentInfo?.coloredLike
            val offsets = ctx.getOrNull("offsets")?.runParser(TextureOffsets.Parser)
                ?: parentInfo?.offsets

            return if (textureSource == null) {
                Partial(
                    textureSource,
                    coloredLike,
                    offsets
                )
            } else {
                Complete(
                    textureSource,
                    coloredLike,
                    offsets
                )
            }
        }
    }

    data class Complete(public override val textureSource: TextureSource, public override val coloredLike: ColoredLike?, public override val offsets: TextureOffsets?) : OverlayInfo()
    data class Partial(override val textureSource: TextureSource?, override val coloredLike: ColoredLike?, override val offsets: TextureOffsets?) : OverlayInfo()

    protected abstract val textureSource: TextureSource?
    protected abstract val coloredLike: ColoredLike?
    protected abstract val offsets: TextureOffsets?
}

sealed class TextureSource {
    object Parser : JsonParser<TextureSource> {
        override fun invoke(ctx: JsonParseContext) =
            ctx.sumType(
                "single" to Single.Parser,
                "sided" to Sided.Parser
            )
    }

    data class Single(val spriteId: Identifier) : TextureSource() {
        object Parser : JsonParser<Single> {
            override fun invoke(ctx: JsonParseContext) =
                Single(
                    ctx.runParser(
                        IdentifierParser
                    )
                )
        }
    }

    data class Sided(val map: Map<Direction, Identifier>) : TextureSource(), Map<Direction, Identifier> by map {
        object Parser : JsonParser<Sided> {
            override fun invoke(ctx: JsonParseContext) =
                Sided(
                    ctx.runParser(makeSidedMapParserUsing { it["texture"].runParser(IdentifierParser) } )
                )
        }
    }
}

data class ColoredLike(val colorSource: BlockState) {
    object Parser : JsonParser<ColoredLike> {
        override fun invoke(ctx: JsonParseContext) =
            ctx.runParser(IdentifierParser).let { id ->
                ColoredLike(
                    Registry.BLOCK.getOrEmpty(id).orNull()?.defaultState ?: ctx.error("Invalid ID: $id")
                )
            }
    }
}

data class TextureOffsets(val map: Map<Direction, Offsetters>) : Map<Direction, Offsetters> by map {
    object Parser : JsonParser<TextureOffsets> {
        override fun invoke(ctx: JsonParseContext) =
            TextureOffsets(
                makeSidedMapParserUsing(Offsetters.Parser)(
                    ctx
                )
            )
    }
}

sealed class Offsetters {
    object Parser : JsonParser<Offsetters> {
        override fun invoke(ctx: JsonParseContext) =
            ctx.sumType(
                "uOffsetter" to (Offsetter.Parser andThen Offsetters::U),
                "vOffsetter" to (Offsetter.Parser andThen Offsetters::V),
                "uv" to UV.Parser
            )
    }

    data class U(val uOffsetter: Offsetter) : Offsetters()
    data class V(val vOffsetter: Offsetter) : Offsetters()
    data class UV(val uOffsetter: Offsetter, val vOffsetter: Offsetter) : Offsetters() {
        object Parser : JsonParser<UV> {
            override fun invoke(ctx: JsonParseContext) =
                UV(
                    ctx["uOffsetter"].runParser(Offsetter.Parser),
                    ctx["vOffsetter"].runParser(Offsetter.Parser)
                )
        }
    }
}

/**
 * An [Offsetter] can offset the uv coordinates of the overlay.
 */
sealed class Offsetter {
    object Parser : JsonParser<Offsetter> {
        @Suppress("RedundantLambdaArrow")
        override fun invoke(ctx: JsonParseContext) =
            ctx.sumType<Offsetter>(
                "remap" to Remap.Parser,
                "zero" to { _ -> Zero }
            )
    }

    /**
     * "Offets" by performing hard-coded value replacements.
     */
    data class Remap(val map: Map<Float4, Float4>) : Offsetter(), Map<Float4, Float4> by map {
        object Parser : JsonParser<Remap> {
            override fun invoke(ctx: JsonParseContext) =
                Remap(ctx.map { objCtx ->
                    Pair(
                        objCtx["from"].runParser(Float4.Parser),
                        objCtx["to"].runParser(Float4.Parser)
                    )
                }.toMap())
        }
    }

    /**
     * Offsets by converting from the range a..b to 0..(b-a)
     */
    object Zero : Offsetter()
}

data class Float4(val a: Float, val b: Float, val c: Float, val d: Float) {
    object Parser : JsonParser<Float4> {
        override fun invoke(ctx: JsonParseContext) =
            ctx.runParser(arrayOfSize(4, JsonParseContext::float) andThen { Float4(it[0], it[1], it[2], it[3]) })
    }

    @Suppress("MemberVisibilityCanBePrivate")
    val min get() = min(a, min(b, min(c, d)))
    @Suppress("MemberVisibilityCanBePrivate")
    val max get() = max(a, max(b, max(c, d)))
    val center get() = (min + max) / 2

    fun map(f: (Float) -> Float) = Float4(f(a), f(b), f(c), f(d))

    override fun toString() = "[$a, $b, $c, $d]"
}
