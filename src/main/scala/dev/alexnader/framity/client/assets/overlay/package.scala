package dev.alexnader.framity.client.assets

import java.io.{BufferedReader, InputStreamReader}
import java.util.concurrent.{CompletableFuture, Executor}

import com.google.gson.{Gson, JsonElement}
import dev.alexnader.framity.Framity
import dev.alexnader.framity.client.assets.overlay.OverlayInfo.Parser
import dev.alexnader.framity.util.collect.{CollectIterable, Collectors}
import dev.alexnader.framity.util.json.{JsonParseContext, JsonParseFailure, JsonParseResult}
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler

import scala.collection.mutable
import scala.jdk.CollectionConverters._

package object overlay {
  private val overlayInfoMap: mutable.Map[Identifier, OverlayInfo] = mutable.Map()

  implicit class IdentifierOverlayAccess(id: Identifier) {
    def getOverlay: Option[OverlayInfo] = overlayInfoMap.get(id)
  }

  object Listener extends SimpleResourceReloadListener[Iterable[Identifier]] {
    override val getFabricId: Identifier = Framity.Mod.id("listener/assets/overlay")

    private def loadOverlayAndDependencies(resourceManager: ResourceManager, rootOverlayId: Identifier): JsonParseResult[Unit] = {
      val loadedDependencies = mutable.Set[Identifier]()

      def loadOverlay(overlayId: Identifier): JsonParseResult[Unit] = {
        val context = new JsonParseContext(overlayId.toString, new Gson().fromJson(new BufferedReader(new InputStreamReader(resourceManager.getResource(overlayId).getInputStream)), classOf[JsonElement]))

        context.parseDependencies[OverlayInfo]
          .flatMap[JsonParseFailure, Unit](currentDependencies => {
            currentDependencies.map[Either[JsonParseFailure, Unit]](dependency => {
              if (!loadedDependencies.add(dependency)) {
                Left(s"Circular dependency: $dependency and $overlayId.")
              } else {
                loadOverlay(dependency)
              }
            }).collectTo(Collectors.either[JsonParseFailure, Unit])
          }.map(_ => ())) match {
          case e: Left[_, _] => return e
          case Right(_) =>
        }

        overlayInfoMap(overlayId) = context.parseExtended[OverlayInfo](overlayInfoMap.toMap) match {
          case e: Left[_, _] => return e.asInstanceOf[JsonParseResult[Unit]]
          case Right(overlay) => overlay
        }

        Right(())
      }

      if (!(overlayInfoMap contains rootOverlayId)) {
        loadOverlay(rootOverlayId)
      } else {
        Right(())
      }
    }

    override def load(resourceManager: ResourceManager, profiler: Profiler, executor: Executor): CompletableFuture[Iterable[Identifier]] = CompletableFuture.supplyAsync(() => {
      resourceManager.findResources("framity/overlays", _.endsWith(".json")).asScala
    }, executor)

    override def apply(overlayIds: Iterable[Identifier], resourceManager: ResourceManager, profiler: Profiler, executor: Executor): CompletableFuture[Void] = CompletableFuture.runAsync(() => {
      overlayIds foreach { overlayId =>
        loadOverlayAndDependencies(resourceManager, overlayId) match {
          case Left(error) => Framity.Logger.error(s"Error while parsing a Framity overlay: ${error.toException}")
          case Right(()) =>
        }
      }
    }, executor)
  }

}
