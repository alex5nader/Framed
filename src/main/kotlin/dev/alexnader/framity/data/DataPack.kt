package dev.alexnader.framity.data

import dev.alexnader.framity.GSON
import dev.alexnader.framity.LOGGER
import dev.alexnader.framity.data.overlay.json.OverlayDefinition as JsonOverlayDefinition
import dev.alexnader.framity.data.overlay.runtime.OverlayDefinition
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

private val OverlaysMutable: MutableList<OverlayDefinition> = mutableListOf()

fun hasOverlay(stack: ItemStack) = getOverlayInfo(stack) != null

fun getOverlayInfo(stack: ItemStack) =
    OverlaysMutable.find { it.trigger.test(stack) }?.overlayInfo

data class FramityResourceData(val overlays: Collection<Identifier>)

class FramityResourceListener : SimpleResourceReloadListener<FramityResourceData> {
    companion object {
        private val ID = Identifier("framity", "resource_listener")
    }

    override fun getFabricId() = ID

    override fun load(
        manager: ResourceManager,
        profiler: Profiler,
        executor: Executor
    ): CompletableFuture<FramityResourceData> {
        return CompletableFuture.supplyAsync(Supplier {
            OverlaysMutable.clear()

            val overlays = manager.findResources("framity/overlays") { s -> s.endsWith(".json") }

            FramityResourceData(overlays)
        }, executor)
    }

    override fun apply(
        data: FramityResourceData,
        manager: ResourceManager,
        profiler: Profiler,
        executor: Executor
    ): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            data.overlays.forEach { id ->
                try {
                    val input = manager.getResource(id).inputStream
                    val reader = BufferedReader(InputStreamReader(input))

                    val parsed = GSON.fromJson(reader, JsonOverlayDefinition::class.java)

                    OverlaysMutable.add(OverlayDefinition.fromJson(parsed))
                } catch (e: IOException) {
                    LOGGER.error("Error while loading a Framity Overlay JSON ($id): $e")
                }
            }
        }
    }
}
