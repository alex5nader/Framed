package dev.alexnader.framity.client.assets.overlay

import dev.alexnader.framity.util.json.Parsers.IdentifierParser
import dev.alexnader.framity.util.json.{JsonParseContext, JsonParseResult, JsonParser}
import net.minecraft.block.BlockState
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

import scala.jdk.OptionConverters._

object ColoredLike {
  implicit object Parser extends JsonParser[ColoredLike] {
    override def apply(context: JsonParseContext): JsonParseResult[ColoredLike] = {
      context.parse[Identifier].flatMap(id =>
        Registry.BLOCK.getOrEmpty(id)
          .toScala
          .map(block => Right(block.getDefaultState))
          .getOrElse[JsonParseResult[BlockState]](Left(s"Invalid ID: $id"))
          .map(new ColoredLike(_))
      )
    }
  }
}

case class ColoredLike(colorSource: BlockState)
