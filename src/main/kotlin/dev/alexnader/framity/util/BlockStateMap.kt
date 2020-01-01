package dev.alexnader.framity.util

import net.minecraft.block.BlockState
import net.minecraft.state.property.Property

class BlockStateMap<V> : MutableMap<BlockState, V> {
    private val valuesMap: MutableMap<Int, V> = mutableMapOf()
    private val keysMap: MutableMap<Int, BlockState> = mutableMapOf()
    private val properties: MutableSet<Property<Comparable<Any>>> = mutableSetOf()

    override val size get() = valuesMap.size

    @Suppress("UNCHECKED_CAST")
    fun <P : Comparable<P>>includeProperty(prop: Property<P>) {
        this.properties.add(prop as Property<Comparable<Any>>)
    }

    fun excludeProperty(prop: Property<*>) {
        this.properties.remove(prop)
    }

    private fun hash(state: BlockState): Int {
        return this.properties.map { state.get<Comparable<Any>>(it) }.hashCode()
    }

    override fun containsKey(key: BlockState) = keysMap.containsKey(hash(key))

    override fun containsValue(value: V) = valuesMap.containsValue(value)

    override fun get(key: BlockState) = valuesMap[hash(key)]

    override fun isEmpty() = valuesMap.isEmpty()

    override val entries: MutableSet<MutableMap.MutableEntry<BlockState, V>> = keysMap.entries.zip(valuesMap.entries).map { (k, v) -> Entry(k.value, v.value) as MutableMap.MutableEntry<BlockState, V> }.toMutableSet()

    override val keys: MutableSet<BlockState> = keysMap.values.toMutableSet()
    override val values: MutableCollection<V> = valuesMap.values

    override fun clear() = valuesMap.clear()

    override fun put(key: BlockState, value: V): V? {
        val hash = hash(key)
        keysMap[hash] = key
        return valuesMap.put(hash, value)
    }

    override fun putAll(from: Map<out BlockState, V>) {
        for ((k, v) in from) {
            val hash = hash(k)
            keysMap[hash] = k
            valuesMap[hash] = v
        }
    }

    override fun remove(key: BlockState): V? {
        val hash = hash(key)
        keysMap.remove(hash)
        return valuesMap.remove(hash)
    }

    data class Entry<V>(override var key: BlockState, override var value: V) : MutableMap.MutableEntry<BlockState, V> {
        override fun setValue(newValue: V): V {
            val old = value
            value = newValue
            return old
        }
    }
}