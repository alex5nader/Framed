package dev.alexnader.framity.data

import dev.alexnader.framity.util.JsonParseContext
import dev.alexnader.framity.util.JsonParser
import net.minecraft.recipe.Ingredient

object IngredientParser : JsonParser<Ingredient> {
    override fun invoke(ctx: JsonParseContext): Ingredient =
        ctx.wrapErrors { Ingredient.fromJson(ctx.json) }
}

data class OverlayTrigger(val trigger: Ingredient) {
    object Parser : JsonParser<OverlayTrigger> {
        override fun invoke(ctx: JsonParseContext) =
            OverlayTrigger(
                ctx.runParserOnMember(
                    "trigger",
                    IngredientParser
                )
            )
    }
}
