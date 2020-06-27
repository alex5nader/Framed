package dev.alexnader.framity.data

import com.google.gson.JsonElement
import dev.alexnader.framity.GSON
import dev.alexnader.framity.LOGGER
import dev.alexnader.framity.data.overlay.OverlayTrigger
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener
import net.minecraft.item.ItemStack
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

data class FramityData(val overlayIds: Collection<Identifier>)

class FramityDataListener : SimpleResourceReloadListener<FramityData> {
    companion object {
        private val ID = Identifier("framity", "data_listener")
    }

    override fun getFabricId() = ID

    override fun load(
        manager: ResourceManager,
        profiler: Profiler,
        executor: Executor
    ): CompletableFuture<FramityData> {
        return CompletableFuture.supplyAsync(Supplier {
            OverlayTriggers.clear()

            val overlays = manager.findResources("framity/overlays") { s -> s.endsWith(".json") }

            FramityData(overlays)
        }, executor)
    }

    override fun apply(
        data: FramityData,
        manager: ResourceManager,
        profiler: Profiler,
        executor: Executor
    ): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            data.overlayIds.forEach { overlayId ->
                try {
                    val input = manager.getResource(overlayId).inputStream
                    val reader = BufferedReader(InputStreamReader(input))

                    val element = GSON.fromJson(reader, JsonElement::class.java)

                    val ctx = JsonParseContext(overlayId.toString(), element)

                    OverlayTriggers.add(Pair(OverlayTrigger.fromJson(ctx), overlayId))
                } catch (e: IOException) {
                    LOGGER.error("Error while loading an Ingredient JSON ($overlayId): $e")
                }
            }
        }
    }
}
