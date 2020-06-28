package dev.alexnader.framity.data

import com.google.gson.JsonElement
import dev.alexnader.framity.GSON
import dev.alexnader.framity.LOGGER
import dev.alexnader.framity.data.overlay.OverlayInfo
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.function.Supplier

private val OverlayInfoMap: MutableMap<Identifier, OverlayInfo> = mutableMapOf()

fun getOverlay(id: Identifier?) =
    id?.let { OverlayInfoMap[it] }

data class FramityAssets(val overlayIds: Collection<Identifier>)

class FramityAssetsListener : SimpleResourceReloadListener<FramityAssets> {
    companion object {
        private val ID = Identifier("framity", "assets_listener")
    }

    override fun getFabricId() = ID

    override fun load(
        manager: ResourceManager,
        profiler: Profiler,
        executor: Executor
    ): CompletableFuture<FramityAssets> =
        CompletableFuture.supplyAsync(Supplier {
            OverlayInfoMap.clear()

            val overlayIds = manager.findResources("framity/overlays") { s -> s.endsWith(".json") }

            FramityAssets(overlayIds)
        }, executor)

    override fun apply(
        assets: FramityAssets,
        manager: ResourceManager,
        profiler: Profiler,
        executor: Executor
    ): CompletableFuture<Void> = CompletableFuture.runAsync {
        assets.overlayIds.forEach { overlayId ->
            try {
                val input = manager.getResource(overlayId).inputStream
                val reader = BufferedReader(InputStreamReader(input))

                val element = GSON.fromJson(reader, JsonElement::class.java)

                val ctx = JsonParseContext(overlayId.toString(), element)

                OverlayInfoMap[overlayId] = OverlayInfo.fromJson(ctx)
            } catch (e: IOException) {
                LOGGER.error("Error while loading a Framity overlay: $e")
            } catch (e: JsonParseException) {
                LOGGER.error("Error while parsing a Framity overlay: $e")
            }
        }
    }
}
