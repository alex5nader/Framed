package dev.alexnader.framity.data

import java.io.{BufferedReader, IOException, InputStreamReader}
import java.util.concurrent.{CompletableFuture, Executor}

import com.google.gson.{Gson, JsonElement}
import dev.alexnader.framity.Framity
import dev.alexnader.framity.util.json.{JsonParseContext, JsonParser}
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler

import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters._

package object overlay {
  implicit class ItemStackOverlayQuery(stack: ItemStack) {
    def hasOverlay: Boolean = overlay.hasOverlay(stack)
    def getOverlayId: Option[Identifier] = overlay.getOverlayId(stack)
  }

  private val triggers: ArrayBuffer[(Ingredient, Identifier)] = ArrayBuffer()

  def hasOverlay(stack: ItemStack): Boolean = getOverlayId(stack).isDefined
  def getOverlayId(stack: ItemStack): Option[Identifier] = triggers.iterator find (_._1.test(stack)) map (_._2)

  object Listener extends SimpleResourceReloadListener[Iterable[Identifier]] {
    private implicit val triggerIngredientParser: JsonParser[Ingredient] =
      JsonParser.fieldOf("trigger")(dev.alexnader.framity.util.json.Parsers.IngredientParser)

    override val getFabricId: Identifier = Framity.Mod.id("listener/data/overlay")

    override def load(resourceManager: ResourceManager, profiler: Profiler, executor: Executor): CompletableFuture[Iterable[Identifier]] = CompletableFuture.supplyAsync(() => {
      triggers.clear()

      resourceManager.findResources("framity/overlays", _.endsWith(".json")).asScala
    }, executor)

    override def apply(data: Iterable[Identifier], resourceManager: ResourceManager, profiler: Profiler, executor: Executor): CompletableFuture[Void] = CompletableFuture.runAsync(() => {
      data foreach { overlayId =>
        try {
          val input = resourceManager.getResource(overlayId).getInputStream
          val reader = new BufferedReader(new InputStreamReader(input))

          val ctx = new JsonParseContext(overlayId.toString, new Gson().fromJson(reader, classOf[JsonElement]))

          ctx.parse[Ingredient] match {
            case Right(ingredient) => triggers.addOne(ingredient, overlayId)
            case Left(error) => Framity.Logger.error(s"Error while parsing a Framity overlay: ${error.toException}")
          }
        } catch {
          case e: IOException => Framity.Logger.error(s"Error while loading a Framity overlay: $e")
        }
      }
    }, executor)
  }
}
