@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.alexnader.framity.util.json

import com.google.gson.*

class JsonParseException(message: String) : Exception(message)

class JsonParseContext private constructor(private val label: String, val json: JsonElement, private val path: String) {
    constructor(label: String, json: JsonElement) : this(label, json, "")

    operator fun get(key: String): JsonParseContext {
        val json = this.obj[key] ?: this.error("Expected key $key.")
        return JsonParseContext(
            this.label,
            json,
            "${this.path}.$key"
        )
    }

    operator fun get(index: Int): JsonParseContext {
        val arr = this.arr
        if (index >= arr.size()) {
            this.error("Expected at least ${index + 1} elements.")
        }
        val json = this.arr[index]
        return JsonParseContext(
            this.label,
            json,
            "${this.path}[$index]"
        )
    }

    fun getOrNull(key: String) =
        json.asJsonObject?.let { obj ->
            obj[key]?.let { json ->
                JsonParseContext(this.label, json, "${this.path}.$key")
            }
        }

    fun getOrNull(index: Int) =
        json.asJsonArray?.let { arr ->
            if (0 <= index && index < arr.size()) {
                JsonParseContext(this.label, arr[index], "${this.path}[$index]")
            } else {
                null
            }
        }

    fun getKey(key: String) =
        JsonParseContext(
            this.label,
            JsonPrimitive(key),
            "\"$key\" in ${this.path}"
        )

    private fun <J, T> getJsonValue(getter: (J) -> T, arg: J, errorMsg: String) =
        try {
            getter(arg)
        } catch (e: Exception) {
            e.printStackTrace()
            this.error(errorMsg)
        }

    val obj: JsonObject get() = getJsonValue(JsonElement::getAsJsonObject, this.json, "Expected an object.")
    val arr: JsonArray get() = getJsonValue(JsonElement::getAsJsonArray, this.json, "Expected an array.")
    val primitive: JsonPrimitive get() = getJsonValue(JsonElement::getAsJsonPrimitive, this.json, "Expected a primitive.")
    val jsonNull: JsonNull get() = getJsonValue(JsonElement::getAsJsonNull, this.json, "Expected null")
    val bool get() = getJsonValue(JsonPrimitive::getAsBoolean, this.primitive, "Expected a boolean.")
    val number: Number get() = getJsonValue(JsonPrimitive::getAsNumber, this.primitive, "Expected a number.")
    val string: String get() = getJsonValue(JsonPrimitive::getAsString, this.primitive, "Expected a string.")
    val double get() = getJsonValue(JsonPrimitive::getAsDouble, this.primitive, "Expected a double.")
    val float get() = getJsonValue(JsonPrimitive::getAsFloat, this.primitive, "Expected a float.")
    val long get() = getJsonValue(JsonPrimitive::getAsLong, this.primitive, "Expected a long.")
    val int get() = getJsonValue(JsonPrimitive::getAsInt, this.primitive, "Expected an int.")
    val byte get() = getJsonValue(JsonPrimitive::getAsByte, this.primitive, "Expected a byte.")
    val char get() = getJsonValue(JsonPrimitive::getAsCharacter, this.primitive, "Expected a char.")
    val short get() = getJsonValue(JsonPrimitive::getAsShort, this.primitive, "Expected a short.")

    fun error(msg: String): Nothing =
        throw JsonParseException("Error from ${this.label} at ${if (this.path.isEmpty()) "<root>" else this.path}: $msg")

    fun <R> sumType(vararg cases: Pair<String, JsonParser<R>>): R {
        val obj = this.obj
        val (key, parser) = cases.find { (key, _) ->
            obj.has(key)
        } ?: this.error("Expected one of: ${cases.map(Pair<*, *>::first)}")
        return this[key].runParser(parser)
    }

    fun <R> map(parser: JsonParser<R>) =
        (0 until this.arr.size()).map { this[it].runParser(parser) }

    fun <R> flatMap(parser: JsonParser<Iterable<R>>) =
        (0 until this.arr.size()).flatMap { this[it].runParser(parser) }

    fun heteroArray(vararg parser: JsonParser<Any>) =
        (0 until this.arr.size()).map { this[it].runParser(parser[it]) }

    fun <K, V> dictionary(keyParser: JsonParser<K>, valueParser: JsonParser<V>): Map<K, V> =
        this.obj.keySet().associate { key ->
            Pair(getKey(key).runParser(keyParser), this[key].runParser(valueParser))
        }

    fun <R> runParser(parser: JsonParser<R>) = parser(this)

    fun <R> wrapErrors(block: JsonParseContext.() -> R) =
        try {
            this.block()
        } catch (e: Exception) {
            this.error("Error while parsing: $e")
        }

    fun <R> firstNonNull(vararg parsers: JsonParser<R?>) =
        parsers.asSequence().map { this.runParser(it) }.first { it != null } ?: this.error("No parsers matched.")
}

fun JsonElement.toContext(label: String) =
    JsonParseContext(label, this)

typealias JsonParser<R> = (JsonParseContext) -> R
