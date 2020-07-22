package dev.alexnader.framity.data

import com.google.gson.JsonElement
import dev.alexnader.framity.GSON
import dev.alexnader.framity.LOGGER
import dev.alexnader.framity.MOD
import dev.alexnader.framity.util.json.*
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.function.Supplier

private val OverlayTriggers: MutableList<Pair<OverlayTrigger, Identifier>> = mutableListOf()

fun hasOverlay(stack: ItemStack) = getOverlayId(stack) != null

fun getOverlayId(stack: ItemStack) =
    OverlayTriggers.find { (trigger, _) -> trigger.trigger.test(stack) }?.second

object OverlayDataListener : SimpleResourceReloadListener<Collection<Identifier>> {
    private val id = MOD.id("listener/data/overlay")
    override fun getFabricId() = id

    override fun load(
        manager: ResourceManager,
        profiler: Profiler,
        executor: Executor
    ): CompletableFuture<Collection<Identifier>> {
        return CompletableFuture.supplyAsync(Supplier {
            OverlayTriggers.clear()

            manager.findResources("framity/overlays") { s -> s.endsWith(".json") }
        }, executor)
    }

    override fun apply(
        data: Collection<Identifier>,
        manager: ResourceManager,
        profiler: Profiler,
        executor: Executor
    ): CompletableFuture<Void> = CompletableFuture.runAsync(Runnable {
        data.forEach { overlayId ->
            try {
                val input = manager.getResource(overlayId).inputStream
                val reader = BufferedReader(InputStreamReader(input))

                val ctx = GSON.fromJson(reader, JsonElement::class.java).toContext(overlayId.toString())

                OverlayTriggers.add(Pair(ctx.runParser(OverlayTrigger.Parser), overlayId))
            } catch (e: IOException) {
                LOGGER.error("Error while loading a Framity overlay: $e")
            } catch (e: JsonParseException) {
                LOGGER.error("Error while parsing a Framity overlay: $e")
            }
        }
    }, executor)
}

data class OverlayTrigger(val trigger: Ingredient) {
    object Parser : JsonParser<OverlayTrigger> {
        override fun invoke(ctx: JsonParseContext) =
            OverlayTrigger(
                ctx["trigger"].runParser(IngredientParser)
            )
    }
}
