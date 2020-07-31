package dev.alexnader.framity.util.json

import dev.alexnader.framity.util.collect.{CollectIterable, Collectors}
import dev.alexnader.framity.util.json.dsl._
import net.minecraft.recipe.Ingredient
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction

object Parsers {
  implicit val IdentifierParser: JsonParser[Identifier] = context =>
    context.asString flatMap { s => context.catchErrors(new Identifier(s)) }

  implicit val DirectionParser: JsonParser[Direction] = context =>
    context.asString map Direction.byName flatMap {
      case null => Left(context.makeFailure("Invalid direction."))
      case direction => Right(direction)
    }

  implicit def sidedMap[A](implicit parser: JsonParser[A]): JsonParser[Map[Direction, A]] = {
    val itemParser: JsonParser[(Seq[Direction], A)] =
      parse field "sides" using ((parse seq) using DirectionParser) pairWith parser

    (parse seq) using itemParser mapResult { _ flatMap { case (dirs, tex) => dirs map ((_, tex)) } toMap }
  }

  implicit val IngredientParser: JsonParser[Ingredient] = context => context catchErrors Ingredient.fromJson(context json)

  def arrayOfSize[A](size: Int)(implicit parser: JsonParser[A]): JsonParser[Seq[A]] = context => {
    context.asArr flatMap { arr =>
      if (arr.size != size) {
        Left(context.makeFailure(s"Expected an array of $size elements."))
      } else {
        context.arrayItems.flatMap(items => items.map(_.parse[A]).collectTo(Collectors.either))
      }
    }
  }
}
