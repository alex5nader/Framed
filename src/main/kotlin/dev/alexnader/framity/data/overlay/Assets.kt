package dev.alexnader.framity.data.overlay

import dev.alexnader.framity.data.*
import dev.alexnader.framity.util.*
import net.minecraft.block.BlockState
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry
import kotlin.math.max
import kotlin.math.min

private object DirectionParser : JsonParser<Direction> {
    override fun invoke(ctx: JsonParseContext) =
        when (ctx.string) {
            "down" -> Direction.DOWN
            "up" -> Direction.UP
            "north" -> Direction.NORTH
            "south" -> Direction.SOUTH
            "east" -> Direction.EAST
            "west" -> Direction.WEST
            else -> ctx.error("Invalid direction.")
        }
}

private object IdentifierParser : JsonParser<Identifier> {
    override fun invoke(ctx: JsonParseContext) =
        Identifier(ctx.string)
}

private fun <T> makeSidedMapParserUsing(parser: JsonParser<T>) =
    { arrCtx: JsonParseContext ->
        arrCtx.flatMap { objCtx ->
            val value = objCtx.runParserOnMember("value", parser)
            objCtx.getMember("sides").map { sideCtx ->
                Pair(sideCtx.runParser(DirectionParser), value)
            }
        }.toMap()
    }

sealed class OverlayInfo {
    object DependenciesParser : JsonParser<List<Identifier>> {
        override fun invoke(ctx: JsonParseContext): List<Identifier> {
            val parent = ctx.runParserOnNullableMember("parent", IdentifierParser)

            return parent?.let { listOf(it) } ?: listOf()
        }
    }

    object Parser : JsonParser<OverlayInfo> {
        override fun invoke(ctx: JsonParseContext): OverlayInfo {
            val parent = ctx.runParserOnNullableMember("parent", IdentifierParser)
            val parentInfo = parent?.let { getOverlay(it) }

            val textureSource = ctx.runParserOnNullableMember("textureSource", TextureSource.Parser)
                ?: parentInfo?.textureSource
            val coloredLike = ctx.runParserOnNullableMember("coloredLike", ColoredLike.Parser)
                ?: parentInfo?.coloredLike
            val offsets = ctx.runParserOnNullableMember("offsets", TextureOffsets.Parser)
                ?: parentInfo?.offsets

            return if (textureSource == null) {
                Partial(textureSource, coloredLike, offsets)
            } else {
                Complete(textureSource, coloredLike, offsets)
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
                Single(ctx.runParser(IdentifierParser))
        }
    }

    data class Sided(val map: Map<Direction, Identifier>) : TextureSource(), Map<Direction, Identifier> by map {
        object Parser : JsonParser<Sided> {
            override fun invoke(ctx: JsonParseContext) =
                Sided(makeSidedMapParserUsing(IdentifierParser)(ctx))
        }
    }
}

data class ColoredLike(val colorSource: BlockState) {
    object Parser : JsonParser<ColoredLike> {
        override fun invoke(ctx: JsonParseContext) =
            ctx.runParser(IdentifierParser).let { id ->
                ColoredLike(Registry.BLOCK.getOrEmpty(id).orNull()?.defaultState ?: ctx.error("Invalid ID: $id"))
            }
    }
}

data class TextureOffsets(val map: Map<Direction, Offsetters>) : Map<Direction, Offsetters> by map {
    object Parser : JsonParser<TextureOffsets> {
        override fun invoke(ctx: JsonParseContext) =
            TextureOffsets(makeSidedMapParserUsing(Offsetters.Parser)(ctx))
    }
}

sealed class Offsetters {
    object Parser : JsonParser<Offsetters> {
        override fun invoke(ctx: JsonParseContext) =
            ctx.sumType(
                "uOffsetter" to (Offsetter.Parser andThen ::U),
                "vOffsetter" to (Offsetter.Parser andThen ::V),
                "uv" to UV.Parser
            )
    }

    data class U(val uOffsetter: Offsetter) : Offsetters()
    data class V(val vOffsetter: Offsetter) : Offsetters()
    data class UV(val uOffsetter: Offsetter, val vOffsetter: Offsetter) : Offsetters() {
        object Parser : JsonParser<UV> {
            override fun invoke(ctx: JsonParseContext) =
                UV(
                    ctx.runParserOnMember("uOffsetter", Offsetter.Parser),
                    ctx.runParserOnMember("vOffsetter", Offsetter.Parser)
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
                        objCtx.runParserOnMember("from", Float4.Parser),
                        objCtx.runParserOnMember("to", Float4.Parser)
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
            ctx.map(JsonParseContext::float).let { list ->
                if (list.size != 4) {
                    ctx.error("Expected 4 elements.")
                } else {
                    Float4(list[0], list[1], list[2], list[3])
                }
            }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    val min get() = min(a, min(b, min(c, d)))
    @Suppress("MemberVisibilityCanBePrivate")
    val max get() = max(a, max(b, max(c, d)))
    val center get() = (min + max) / 2

    fun map(f: (Float) -> Float) = Float4(f(a), f(b), f(c), f(d))

    override fun toString() = "[$a, $b, $c, $d]"
}
