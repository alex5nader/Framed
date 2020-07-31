package dev.alexnader.framity.util.json

import dev.alexnader.framity.util.collect.{CollectIterable, Collectors}

package object dsl {
  object parse {
    class FieldParser(field: String) extends JsonParser[JsonParseContext] {
      def using[B](implicit adapter: JsonParser[B]): JsonParser[B] = _.parse(this).flatMap(_.parse[B])

      override def apply(context: JsonParseContext): JsonParseResult[JsonParseContext] = context(field)
    }

    def field(field: String): FieldParser = new FieldParser(field)

    class SeqParser extends JsonParser[Seq[JsonParseContext]] {
      def using[A](implicit adapter: JsonParser[A]): JsonParser[Seq[A]] = _.parse(this).flatMap(_.map(_.parse[A]).collectTo(Collectors.either))

      override def apply(context: JsonParseContext): JsonParseResult[Seq[JsonParseContext]] = context.arrayItems
    }

    val seq: SeqParser = new SeqParser
  }
}
