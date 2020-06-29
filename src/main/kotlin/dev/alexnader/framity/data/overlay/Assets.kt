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

sealed class OverlayInfo {
    companion object {
        fun dependencies(ctx: JsonParseContext): List<Identifier> {
            val parent = ctx.getChildOrNullWith("parent", ::identifierFromJson)

            return parent?.let { listOf(it) } ?: listOf()
        }

        fun fromJson(ctx: JsonParseContext): OverlayInfo {
            val parent = ctx.getChildOrNullWith("parent", ::identifierFromJson)
            val parentInfo = parent?.let { getOverlay(it) }

            val textureSource = ctx.getChildOrNullWith("textureSource", TextureSource.Companion::fromJson)
                ?: parentInfo?.textureSource
            val coloredLike = ctx.getChildOrNullWith("coloredLike", ColoredLike.Companion::fromJson)
                ?: parentInfo?.coloredLike
            val offsets = ctx.getChildOrNullWith("offsets", TextureOffsets.Companion::fromJson)
                ?: parentInfo?.offsets

            return if (textureSource == null) {
                Parent(textureSource, coloredLike, offsets)
            } else {
                Valid(textureSource, coloredLike, offsets)
            }
        }
    }

    data class Valid(public override val textureSource: TextureSource, public override val coloredLike: ColoredLike?, public override val offsets: TextureOffsets?) : OverlayInfo()
    data class Parent(override val textureSource: TextureSource?, override val coloredLike: ColoredLike?, override val offsets: TextureOffsets?) : OverlayInfo()

    protected abstract val textureSource: TextureSource?
    protected abstract val coloredLike: ColoredLike?
    protected abstract val offsets: TextureOffsets?
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
            ctx.sumType<Offsetter>(
                "remap" to Remap.Companion::fromJson,
                "zero" to { _ -> Zero }
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

    /**
     * Offsets by converting from the range a..b to 0..(b-a)
     */
    object Zero : Offsetter()
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
