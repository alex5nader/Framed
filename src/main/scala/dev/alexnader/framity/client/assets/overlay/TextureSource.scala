package dev.alexnader.framity.client.assets.overlay

import dev.alexnader.framity.util.json.JsonParser
import dev.alexnader.framity.util.json.Parsers.sidedMap
import dev.alexnader.framity.util.json.dsl.parse
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction

object TextureSource {

  implicit val Parser: JsonParser[TextureSource] = JsonParser.sumType(
    "single" -> Single.Parser,
    "sided" -> Sided.Parser
  )

  object Single {
    import dev.alexnader.framity.util.json.Parsers.IdentifierParser

    implicit val Parser: JsonParser[Single] = _.parse[Identifier] map Single.apply
  }

  case class Single(spriteId: Identifier) extends TextureSource

  object Sided {
    private implicit val identifierParser: JsonParser[Identifier] = {
      import dev.alexnader.framity.util.json.Parsers.IdentifierParser
      parse field "texture" using IdentifierParser
    }

    implicit val Parser: JsonParser[Sided] = _.parse[Map[Direction, Identifier]] map Sided.apply
  }

  case class Sided(map: Map[Direction, Identifier]) extends TextureSource

}

sealed abstract class TextureSource
