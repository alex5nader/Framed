package dev.alexnader.framity.util.json.dsl

import dev.alexnader.framity.util.UnConsTuple.unCons3
import dev.alexnader.framity.util.json.JsonParser

object Test {

  case class ObjectTest(string: String, num: Int, boolean: Boolean)

  implicit val ObjectTestParser: JsonParser[ObjectTest] =
    parse field "string" using { _.asString } pairWith
      (parse field "num" using { _.asInt }) pairWith
      (parse field "bool" using { _.asBool }) mapResult (ObjectTest.tupled(_))

  case class ArrayTest(vals: Seq[Int])

  implicit val ArrayTestParser: JsonParser[ArrayTest] =
    (parse seq) using { _.asInt } mapResult ArrayTest
}
