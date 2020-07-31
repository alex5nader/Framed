package dev.alexnader.framity.util

import dev.alexnader.framity.util.collect.{CollectIterable, Collectors}

package object json {
  type JsonParseResult[+A] = Either[JsonParseFailure, A]

  trait JsonParser[+A] extends (JsonParseContext => JsonParseResult[A]) {
    def pairWith[B](implicit other: JsonParser[B]): JsonParser[(A, B)] = context =>
      context.parse(this) flatMap { resA => context.parse[B] map { (resA, _ ) } }

    def mapResult[B](f: A => B): JsonParser[B] = context => context.parse(this).map(f)

    def flatMapResult[B](f: A => JsonParseResult[B]): JsonParser[B] = _.parse(this).flatMap(f)
  }

  object JsonParser {
    def sumType[A](cases: (String, JsonParser[A])*)(context: JsonParseContext): JsonParseResult[A] = {
      context.asObj.flatMap(obj => {
        val (key, parser) = cases.find { case (key, _) => obj.has(key) } match {
          case Some(pair) => pair
          case None => return Left(context.makeFailure(s"Expected one of: ${cases.map(_._1)}"))
        }
        context(key).flatMap(parser)
      })
    }

    implicit def tuple4[A](implicit parser: JsonParser[A]): JsonParser[(A, A, A, A)] = context => {
      (0 until 4).map(context.apply).map(item => {
        item.flatMap(itemCtx => itemCtx.parse(parser))
      }).collectTo(Collectors.either).flatMap(s => {
        if (s.size != 4) {
          Left("Expected 4 elements.")
        } else {
          Right(s.head, s(1), s(2), s(3))
        }
      })
    }

    def succeed[A](a: => A): JsonParser[A] = _ => Right(a)
    def fail[A](failure: => JsonParseFailure): JsonParser[A] = _ => Left(failure)
  }

  implicit class ResultExt[A](result: JsonParseResult[A]) {
    def unwrap: A = result.getOrElse(throw new RuntimeException(s"Cannot unwrap err result: ${result.left.getOrElse(throw UnreachableException)}."))
  }
}
