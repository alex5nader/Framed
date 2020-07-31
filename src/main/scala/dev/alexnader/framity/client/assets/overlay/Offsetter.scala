package dev.alexnader.framity.client.assets.overlay

import dev.alexnader.framity.util.ScalaExtensions.MinMax
import dev.alexnader.framity.util.json.JsonParser
import dev.alexnader.framity.util.json.dsl.parse

object Offsetter {

  implicit val Parser: JsonParser[Offsetter] = JsonParser.sumType(
    "remap" -> Offsetter.Remap.Parser,
    "zero" -> JsonParser.succeed(Offsetter.Zero)
  )

  object Remap {

    implicit val Parser: JsonParser[Remap] = {
      val float4Parser: JsonParser[(Float, Float, Float, Float)] = JsonParser.tuple4 { _.asFloat }
      val itemParser: JsonParser[((Float, Float, Float, Float), (Float, Float, Float, Float))] =
        parse field "from" using float4Parser pairWith
          (parse field "to" using float4Parser)

      (parse seq) using itemParser mapResult { pairs => Remap(pairs.toMap) }
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
