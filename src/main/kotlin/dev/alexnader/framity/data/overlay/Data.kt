package dev.alexnader.framity.data.overlay

import dev.alexnader.framity.data.JsonParseContext
import net.minecraft.recipe.Ingredient

fun ingredientFromJson(ctx: JsonParseContext) =
    try {
        Ingredient.fromJson(ctx.json)
    } catch (e: Exception) {
        ctx.error("Error while parsing ingredient: $e")
    }

data class OverlayTrigger(val trigger: Ingredient) {
    companion object {
        fun fromJson(ctx: JsonParseContext) =
            OverlayTrigger(ctx.getChildWith("trigger", ::ingredientFromJson))
    }
}
