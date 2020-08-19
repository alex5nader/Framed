package dev.alexnader.framity.client.assets.overlay

import dev.alexnader.framity.util.ScalaExtensions.MinMax
import dev.alexnader.framity.util.json.JsonParser
import dev.alexnader.framity.util.json.JsonParser.tuple4

import scala.util.chaining._

object Offsetter {

  implicit val Parser: JsonParser[Offsetter] = JsonParser.sumType(
    "remap" -> Offsetter.Remap.Parser,
    "zero" -> JsonParser.succeed(Offsetter.Zero)
  )

  object Remap {

    implicit val Parser: JsonParser[Remap] = {
      val itemParser: JsonParser[((Float, Float, Float, Float), (Float, Float, Float, Float))] = {
        implicit val floatParser: JsonParser[Float] = _.asFloat
        JsonParser.fieldOf[(Float, Float, Float, Float)]("from") pairWith
          JsonParser.fieldOf("to")
      }

      JsonParser.seqOf(itemParser) mapResult { _.toMap.pipe(Remap.apply) }
    }

  }

  case class Remap(map: Map[(Float, Float, Float, Float), (Float, Float, Float, Float)]) extends Offsetter {
    override def apply(orig: (Float, Float, Float, Float)): (Float, Float, Float, Float) = map.getOrElse(orig, orig)
  }

  object Zero extends Offsetter {
    override def apply(orig: (Float, Float, Float, Float)): (Float, Float, Float, Float) = {
      val (min, max) = orig._1 minMax orig._2
      val delta = max - min

      if (orig._1 == min) {
        (0f, delta, delta, 0f)
      } else {
        (delta, 0f, 0f, delta)
      }
    }
  }
}

sealed abstract class Offsetter {
  def apply(orig: (Float, Float, Float, Float)): (Float, Float, Float, Float)
}
