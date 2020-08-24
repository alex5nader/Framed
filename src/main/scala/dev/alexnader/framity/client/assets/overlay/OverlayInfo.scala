package dev.alexnader.framity.client.assets.overlay

import dev.alexnader.framity.util.json.Parsers.{IdentifierParser, sidedMap}
import dev.alexnader.framity.util.json.{DependentJsonParser, JsonParseContext, JsonParseResult, JsonParser}
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction

object OverlayInfo {
  implicit object Parser extends DependentJsonParser[OverlayInfo] {
    override def parseDependencies(context: JsonParseContext): JsonParseResult[Iterable[Identifier]] = {
      context("parent") match {
        case Right(parentCtx) => parentCtx.parse[Identifier] map (Iterable(_))
        case Left(_) => Right(Iterable.empty)
      }
    }

    override def run(dependencies: Map[Identifier, OverlayInfo], context: JsonParseContext): JsonParseResult[OverlayInfo] = {
      def optionalFieldParser[A](field: String)(implicit parser: JsonParser[A]): JsonParser[Option[A]] = context => context(field) match {
        case Right(fieldContext) => fieldContext.parse[A].map(x => Some(x))
        case Left(_) => Right(None)
      }

      context.parse(
        optionalFieldParser[OverlayInfo]("parent")(_.parse[Identifier].map(dependencies.apply)) pairWith
        optionalFieldParser[TextureSource]("textureSource") pairWith
        optionalFieldParser[ColoredLike]("coloredLike") pairWith
        optionalFieldParser("offsets")(sidedMap[Offsetters])
      ) map { case (((parent, textureSource), coloredLike), offsets) =>
        OverlayInfo(parent, textureSource, coloredLike, offsets)
      }
    }
  }
}

case class OverlayInfo(parent: Option[OverlayInfo], _textureSource: Option[TextureSource], _coloredLike: Option[ColoredLike], _offsets: Option[Map[Direction, Offsetters]]) {
  private def getFromParent[A](get: OverlayInfo => Option[A]): Option[A] = get(this) match {
    case Some(value) => Some(value)
    case None => parent.flatMap(get)
  }

  def textureSource: Option[TextureSource] = getFromParent(_._textureSource)

  def coloredLike: Option[ColoredLike] = getFromParent(_._coloredLike)

  def offsets: Option[Map[Direction, Offsetters]] = getFromParent(_._offsets)

  def isValid: Boolean = textureSource.isDefined
}
