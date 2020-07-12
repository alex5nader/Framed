@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.alexnader.framity.util

import com.google.gson.*

class JsonParseException(message: String) : Exception(message)

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

    fun getMember(key: String): JsonParseContext {
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
    val jsonNull: JsonNull get() = getAsOrNull(JsonElement::getAsJsonNull, this.json, "Expected null")
    val bool get() = getAsOrNull(JsonPrimitive::getAsBoolean, this.primitive, "Expected a boolean.")
    val number: Number get() = getAsOrNull(JsonPrimitive::getAsNumber, this.primitive, "Expected a number.")
    val string: String get() = getAsOrNull(JsonPrimitive::getAsString, this.primitive, "Expected a string.")
    val double get() = getAsOrNull(JsonPrimitive::getAsDouble, this.primitive, "Expected a double.")
    val float get() = getAsOrNull(JsonPrimitive::getAsFloat, this.primitive, "Expected a float.")
    val long get() = getAsOrNull(JsonPrimitive::getAsLong, this.primitive, "Expected a long.")
    val int get() = getAsOrNull(JsonPrimitive::getAsInt, this.primitive, "Expected an int.")
    val byte get() = getAsOrNull(JsonPrimitive::getAsByte, this.primitive, "Expected a byte.")
    val char get() = getAsOrNull(JsonPrimitive::getAsCharacter, this.primitive, "Expected a char.")
    val short get() = getAsOrNull(JsonPrimitive::getAsShort, this.primitive, "Expected a short.")

    fun error(msg: String): Nothing =
        throw JsonParseException("Error from ${this.label} at ${if (this.path.isEmpty()) "<root>" else this.path}: $msg")

    fun <T> runParserOnMember(key: String, parser: JsonParser<T>): T =
        parser(this.getMember(key))

    fun <T> runParserOnNullableMember(key: String, parser: JsonParser<T>): T? =
        this.getChildOrNull(key)?.let { ctx -> parser(ctx) }

    fun <T> sumType(vararg cases: Pair<String, JsonParser<T>>): T {
        val obj = this.obj
        val (key, parser) = cases.find { (key, _) ->
            obj.has(key)
        } ?: this.error("Expected one of: ${cases.map(Pair<*, *>::first)}")
        return parser(this.getMember(key))
    }

    fun <T> map(parser: JsonParser<T>) =
        (0 until this.arr.size()).map { parser(accessArray(it)) }

    fun <T> flatMap(parser: JsonParser<Iterable<T>>) =
        (0 until this.arr.size()).flatMap { parser(accessArray(it)) }

    fun heteroArray(vararg parser: JsonParser<Any>) =
        (0 until this.arr.size()).map { parser[it](accessArray(it)) }

    fun <T> runParser(parser: JsonParser<T>) = parser(this)

    fun <T> wrapErrors(block: JsonParseContext.() -> T) =
        try {
            this.block()
        } catch (e: Exception) {
            this.error("Error while parsing: $e")
        }
}

fun JsonElement.toContext(label: String) = JsonParseContext(label, this)

typealias JsonParser<T> = (JsonParseContext) -> T
