package dev.alexnader.framity.data.overlay

import dev.alexnader.framity.data.*
import dev.alexnader.framity.util.andThen
import dev.alexnader.framity.util.orNull
import net.minecraft.block.BlockState
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry

private fun directionFromJson(ctx: JsonParseContext) =
    when (ctx.string) {
        "down" -> Direction.DOWN
        "up" -> Direction.UP
        "north" -> Direction.NORTH
        "south" -> Direction.SOUTH
        "east" -> Direction.EAST
        "west" -> Direction.WEST
        else -> ctx.error("Invalid direction.")
    }

private fun identifierFromJson(ctx: JsonParseContext) =
    Identifier(ctx.string)

private fun <T> makeSidedMapFromJson(fromJson: FromJson<T>) =
    { arrCtx: JsonParseContext ->
        arrCtx.flatMap { objCtx ->
            val value = objCtx.getChildWith("value", fromJson)
            objCtx.getChild("sides").map { sideCtx ->
                Pair(directionFromJson(sideCtx), value)
            }
        }.toMap()
    }

data class OverlayInfo(val textureSource: TextureSource, val coloredLike: ColoredLike? = null, val offsets: TextureOffsets? = null) {
    companion object {
        fun fromJson(ctx: JsonParseContext) =
            OverlayInfo(
                ctx.getChildWith("textureSource", TextureSource.Companion::fromJson),
                ctx.getChildOrNullWith("coloredLike", ColoredLike.Companion::fromJson),
                ctx.getChildOrNullWith("offsets", TextureOffsets.Companion::fromJson)
            )
    }
}

sealed class TextureSource {
    companion object {
        fun fromJson(ctx: JsonParseContext) =
            ctx.sumType(
                "single" to Single.Companion::fromJson,
                "sided" to Sided.Companion::fromJson
            )
    }

    data class Single(val spriteId: Identifier) : TextureSource() {
        companion object {
            fun fromJson(ctx: JsonParseContext) =
                Single(identifierFromJson(ctx))
        }
    }

    data class Sided(val map: Map<Direction, Identifier>) : TextureSource(), Map<Direction, Identifier> by map {
        companion object {
            fun fromJson(ctx: JsonParseContext) =
                Sided(makeSidedMapFromJson(::identifierFromJson)(ctx))
        }
    }
}

data class ColoredLike(val colorSource: BlockState) {
    companion object {
        fun fromJson(ctx: JsonParseContext) =
            identifierFromJson(ctx).let { id ->
                ColoredLike(Registry.BLOCK.getOrEmpty(id).orNull()?.defaultState ?: ctx.error("Invalid ID: $id"))
            }
    }
}

data class TextureOffsets(val map: Map<Direction, Offsetters>) : Map<Direction, Offsetters> by map {
    companion object {
        fun fromJson(ctx: JsonParseContext) =
            TextureOffsets(makeSidedMapFromJson(Offsetters.Companion::fromJson)(ctx))
    }
}

sealed class Offsetters {
    companion object {
        fun fromJson(ctx: JsonParseContext) =
            ctx.sumType(
                "uOffsetter" to (Offsetter.Companion::fromJson andThen ::U),
                "vOffsetter" to (Offsetter.Companion::fromJson andThen ::V),
                "uv" to UV.Companion::fromJson
            )
    }

    data class U(val uOffsetter: Offsetter) : Offsetters()
    data class V(val vOffsetter: Offsetter) : Offsetters()
    data class UV(val uOffsetter: Offsetter, val vOffsetter: Offsetter) : Offsetters() {
        companion object {
            fun fromJson(ctx: JsonParseContext) =
                UV(
                    ctx.getChildWith("uOffsetter", Offsetter.Companion::fromJson),
                    ctx.getChildWith("vOffsetter", Offsetter.Companion::fromJson)
                )
        }
    }
}

/**
 * An [Offsetter] can offset the uv coordinates of the overlay.
 */
sealed class Offsetter {
    companion object {
        fun fromJson(ctx: JsonParseContext) =
            ctx.sumType(
                "remap" to Remap.Companion::fromJson
            )
    }

    /**
     * "Offets" by performing hard-coded value replacements.
     */
    data class Remap(val map: Map<Float4, Float4>) : Offsetter(), Map<Float4, Float4> by map {
        companion object {
            fun fromJson(ctx: JsonParseContext) =
                Remap(ctx.map { objCtx ->
                    Pair(
                        objCtx.getChildWith("from", Float4.Companion::fromJson),
                        objCtx.getChildWith("to", Float4.Companion::fromJson)
                    )
                }.toMap())
        }
    }
}

data class Float4(val a: Float, val b: Float, val c: Float, val d: Float) {
    companion object {
        fun fromJson(ctx: JsonParseContext) =
            ctx.map(::floatFromJson).let { list ->
                if (list.size != 4) {
                    ctx.error("Expected 4 elements.")
                } else {
                    Float4(list[0], list[1], list[2], list[3])
                }
            }
    }
}
