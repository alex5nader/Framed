package dev.alexnader.framity.util.json

import com.google.gson.{JsonArray, JsonElement, JsonNull, JsonObject, JsonPrimitive}
import net.minecraft.util.Identifier

class JsonParseContext private(label: String, val json: JsonElement, path: String) {
  def this(label: String, json: JsonElement) = this(label, json, "")

  def makeFailure(msg: String): JsonParseFailure = s"Error from $label at ${Some(path).filter(!_.isEmpty).getOrElse("<root>")}: $msg"

  private def getJsonValue[A](valid: => Boolean, value: => A, errorMsg: String): JsonParseResult[A] = {
    if (!valid) {
      Left(this.makeFailure(errorMsg))
    } else {
      Right(value)
    }
  }

  def asObj: JsonParseResult[JsonObject] = getJsonValue(json.isJsonObject, json.getAsJsonObject, "Expected an object.")
  def asArr: JsonParseResult[JsonArray] = getJsonValue(json.isJsonArray, json.getAsJsonArray, "Expected an array.")
  def asPrimitive: JsonParseResult[JsonPrimitive] = getJsonValue(json.isJsonPrimitive, json.getAsJsonPrimitive, "Expected a primitive.")
  def asNull: JsonParseResult[JsonNull] = getJsonValue(json.isJsonNull, json.getAsJsonNull, "Expected null.")
  def asBool: JsonParseResult[Boolean] = asPrimitive flatMap { p => getJsonValue(p.isBoolean, p.getAsBoolean, "Expected a boolean.") }
  def asNumber: JsonParseResult[Number] = asPrimitive flatMap { p => getJsonValue(p.isNumber, p.getAsNumber, "Expected a number.") }
  def asString: JsonParseResult[String] = asPrimitive flatMap { p => getJsonValue(p.isString, p.getAsString, "Expected a string.") }
  def asDouble: JsonParseResult[Double] = asPrimitive flatMap { p => getJsonValue(p.isNumber, p.getAsDouble, "Expected a double.") }
  def asFloat: JsonParseResult[Float] = asPrimitive flatMap { p => getJsonValue(p.isNumber, p.getAsFloat, "Expected a float.") }
  def asLong: JsonParseResult[Long] = asPrimitive flatMap { p => getJsonValue(p.isNumber, p.getAsLong, "Expected a long.") }
  def asInt: JsonParseResult[Int] = asPrimitive flatMap { p => getJsonValue(p.isNumber, p.getAsInt, "Expected an int.") }
  def asByte: JsonParseResult[Byte] = asPrimitive flatMap { p => getJsonValue(p.isNumber, p.getAsByte, "Expected a byte.") }
  def asShort: JsonParseResult[Short] = asPrimitive flatMap { p => getJsonValue(p.isNumber, p.getAsShort, "Expected a short.") }

  private def accessObject(key: String): JsonParseResult[JsonParseContext] =
    asObj map { obj => new JsonParseContext(label, obj.get(key), s"$path.$key") }

  private def accessArray(index: Int): JsonParseResult[JsonParseContext] =
    asArr map { arr => new JsonParseContext(label, arr.get(index), s"$path[$index]") }

  def apply(key: String): JsonParseResult[JsonParseContext] = {
    asObj flatMap { obj =>
      if (obj.has(key)) {
        accessObject(key)
      } else {
        Left(makeFailure(s"Expected key $key."))
      }
    }
  }

  def apply(index: Int): JsonParseResult[JsonParseContext] = {
    asArr flatMap { arr =>
      if (!(0 until arr.size contains index)) {
        Left(makeFailure(s"Expected between 0 and ${index + 1} elements."))
      } else {
        accessArray(index)
      }
    }
  }

  def accessKey(key: String): JsonParseResult[JsonParseContext] = {
    asObj flatMap { obj =>
      if (obj.has(key)) {
        Right(new JsonParseContext(label, new JsonPrimitive(key), s"'$key' in $path"))
      } else {
        Left(makeFailure(s"Expected key $key."))
      }
    }
  }

  def arrayItems: JsonParseResult[Seq[JsonParseContext]] = asArr map { arr => (0 until arr.size).map(i => accessArray(i).unwrap) }

  def parse[A](implicit parser: JsonParser[A]): JsonParseResult[A] = parser(this)

  def parseExtended[A](dependencies: Map[Identifier, A])(implicit parser: DependentJsonParser[A]): JsonParseResult[A] =
    parser.run(dependencies, this)

  def parseDependencies[A](implicit parser: DependentJsonParser[A]): JsonParseResult[Iterable[Identifier]] =
    parser.parseDependencies(this)

  def catchErrors[A](actions: => A): JsonParseResult[A] = {
    try {
      Right(actions)
    } catch {
      case e: Exception => Left(makeFailure(s"Error while parsing: $e"))
    }
  }
}
