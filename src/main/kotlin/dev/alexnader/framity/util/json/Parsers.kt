package dev.alexnader.framity.util.json

import dev.alexnader.framity.util.andThen
import net.minecraft.client.util.math.Vector3f
import net.minecraft.recipe.Ingredient
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction

object IdentifierParser : JsonParser<Identifier> {
    override fun invoke(ctx: JsonParseContext) =
        Identifier(ctx.string)
}

object DirectionParser : JsonParser<Direction> {
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

object AxisParser : JsonParser<Direction.Axis> {
    override fun invoke(ctx: JsonParseContext) =
        Direction.Axis.fromName(ctx.string) ?: ctx.error("Invalid axis.")
}

fun <T> makeSidedMapParserUsing(parser: JsonParser<T>) =
    object : JsonParser<Map<Direction, T>> {
        override fun invoke(ctx: JsonParseContext) =
            ctx.flatMap { objCtx ->
                val value = objCtx["value"].runParser(parser)
                objCtx["sides"].map { sideCtx ->
                    Pair(sideCtx.runParser(DirectionParser), value)
                }
            }.toMap()
    }

object IngredientParser : JsonParser<Ingredient> {
    override fun invoke(ctx: JsonParseContext): Ingredient =
        ctx.wrapErrors { Ingredient.fromJson(ctx.json) }
}

fun <T> arrayOfSize(size: Int, parser: JsonParser<T>) =
    object : JsonParser<List<T>> {
        override fun invoke(ctx: JsonParseContext) =
            if (ctx.arr.size() != size) {
                ctx.error("Expected an array of $size elements.")
            } else {
                ctx.map(parser)
            }
    }

object Vector3fParser : JsonParser<Vector3f> {
    override fun invoke(ctx: JsonParseContext) =
        ctx.runParser(arrayOfSize(3, JsonParseContext::float) andThen { Vector3f(it[0], it[1], it[2]) })
}
