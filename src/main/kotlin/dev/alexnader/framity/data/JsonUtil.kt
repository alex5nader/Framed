package dev.alexnader.framity.data

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import kotlin.error as ktError

class JsonParseException(message: String) : Exception(message)

fun floatFromJson(ctx: JsonParseContext) =
    ctx.float

class JsonParseContext private constructor(private val label: String, val json: JsonElement, private val path: String) {
    constructor(label: String, json: JsonElement) : this(label, json, "")

    private fun accessArray(index: Int): JsonParseContext {
        val arr = this.arr
        if (index >= arr.size()) {
            this.error("Expected at least ${index + 1} elements.")
        }
        val json = this.arr[index]
        return JsonParseContext(this.label, json, "${this.path}[$index]")
    }

    fun getChild(key: String): JsonParseContext {
        val json = this.obj[key] ?: this.error("Expected key $key.")
        return JsonParseContext(this.label, json, "${this.path}.$key")
    }

    fun getChildOrNull(key: String): JsonParseContext? =
        json.asJsonObject?.let { obj ->
            obj[key]?.let { json ->
                JsonParseContext(this.label, json, "${this.path}.$key")
            }
        }

    private fun <J, T> getAsOrNull(getter: (J) -> T, arg: J, errorMsg: String) =
        try {
            getter(arg)
        } catch (e: Exception) {
            e.printStackTrace()
            this.error(errorMsg)
        }

    val obj: JsonObject get() = getAsOrNull(JsonElement::getAsJsonObject, this.json, "Expected an object.")
    val arr: JsonArray get() = getAsOrNull(JsonElement::getAsJsonArray, this.json, "Expected an array.")
    val primitive: JsonPrimitive get() = getAsOrNull(JsonElement::getAsJsonPrimitive, this.json, "Expected a primitive.")
    val jsonNull get() = getAsOrNull(JsonElement::getAsJsonNull, this.json, "Expected null")
    val bool get() = getAsOrNull(JsonPrimitive::getAsBoolean, this.primitive, "Expected a boolean.")
    val number get() = getAsOrNull(JsonPrimitive::getAsNumber, this.primitive, "Expected a number.")
    val string get() = getAsOrNull(JsonPrimitive::getAsString, this.primitive, "Expected a string.")
    val double get() = getAsOrNull(JsonPrimitive::getAsDouble, this.primitive, "Expected a double.")
    val float get() = getAsOrNull(JsonPrimitive::getAsFloat, this.primitive, "Expected a float.")
    val long get() = getAsOrNull(JsonPrimitive::getAsLong, this.primitive, "Expected a long.")
    val int get() = getAsOrNull(JsonPrimitive::getAsInt, this.primitive, "Expected an int.")
    val byte get() = getAsOrNull(JsonPrimitive::getAsByte, this.primitive, "Expected a byte.")
    val char get() = getAsOrNull(JsonPrimitive::getAsCharacter, this.primitive, "Expected a char.")
    val short get() = getAsOrNull(JsonPrimitive::getAsShort, this.primitive, "Expected a short.")

    fun error(msg: String): Nothing =
        throw JsonParseException("Error from ${this.label} at ${if (this.path.isEmpty()) "<root>" else this.path}: $msg")

    fun <T> getChildWith(key: String, fromJson: FromJson<T>): T =
        fromJson(this.getChild(key))

    fun <T> getChildOrNullWith(key: String, fromJson: FromJson<T>): T? =
        this.getChildOrNull(key)?.let { ctx -> fromJson(ctx) }

    fun <T> sumType(vararg cases: Pair<String, FromJson<T>>): T {
        val obj = this.obj
        val (key, parser) = cases.find { (key, _) ->
            obj.has(key)
        } ?: this.error("Expected one of: ${cases.map(Pair<*, *>::first)}")
        return parser(this.getChild(key))
    }

    fun <T> map(fromJson: FromJson<T>) =
        (0 until this.arr.size()).map { fromJson(accessArray(it)) }

    fun <T> flatMap(fromJson: FromJson<Iterable<T>>) =
        (0 until this.arr.size()).flatMap { fromJson(accessArray(it)) }

    fun heteroArray(vararg fromJson: FromJson<Any>) =
        (0 until this.arr.size()).map { fromJson[it](accessArray(it)) }
}

typealias FromJson<T> = (JsonParseContext) -> T
