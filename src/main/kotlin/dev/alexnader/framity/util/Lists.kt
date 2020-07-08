package dev.alexnader.framity.util

data class Section(val start: Int, val end: Int) {
    val size get() = end - start
    val indices get() = start until end

    fun contains(index: Int) = index in start until end
    fun findOffset(absolute: Int) = absolute - start
    fun applyOffset(relative: Int) = start + relative
}

fun List<Section>.getSectionIndex(absoluteIndex: Int) =
    indexOfFirst { it.contains(absoluteIndex) }

interface Equality<T> {
    fun checkEquality(a: T, b: T): Boolean
    val name: String
}

class SectionedList<T>(default: T, private val sections: List<Section>, equality: Equality<T> = equalsEquality()) : FixedSizeList<T>(default, sections.sumBy { it.size }, equality) {
    fun getSection(sectionIndex: Int): FixedSizeList<T> =
        this.sections[sectionIndex]
            .let { section -> this.subList(section.start, section.end) }

    override fun toString() =
        "SectionedList(${sections.joinToString(" | ") { section ->
            section.indices.map { this[it] }.joinToString(", ")
        }})"
}

open class FixedSizeList<T> private constructor(private val elements: MutableList<T?>, private val default: T, final override val size: Int, private val equality: Equality<T>) : MutableList<T> {
    private class Iterator<T>(private val fixedSizeList: FixedSizeList<T>) : MutableIterator<T> {
        private var nextIndex = 0

        override fun hasNext() = nextIndex < fixedSizeList.size
        override fun next() = fixedSizeList.elements[nextIndex++] ?: fixedSizeList.default
        override fun remove() {
            fixedSizeList.elements[nextIndex - 1] = null
        }
    }
    private class ListIterator<T>(private val fixedSizeList: FixedSizeList<T>) : MutableListIterator<T> {
        private var nextIndex = 0
        private var justReturnedIndex = -1

        override fun hasPrevious() = previousIndex() >= 0
        override fun nextIndex() = nextIndex
        override fun previous(): T {
            justReturnedIndex = previousIndex()
            return fixedSizeList.elements[previousIndex()] ?: fixedSizeList.default
        }
        override fun previousIndex() = nextIndex - 1

        override fun add(element: T) {
            if (fixedSizeList.isPresent(previousIndex())) {
                throw UnsupportedOperationException("Cannot add to occupied slot (${previousIndex()}) of FixedSizeList")
            }
            fixedSizeList.elements[previousIndex()] = element
        }

        override fun hasNext() = nextIndex < fixedSizeList.size
        override fun next(): T {
            justReturnedIndex = nextIndex
            return fixedSizeList.elements[nextIndex] ?: fixedSizeList.default
        }

        override fun remove() {
            fixedSizeList.elements[justReturnedIndex] = null
        }

        override fun set(element: T) {
            fixedSizeList.elements[justReturnedIndex] = element
        }
    }

    companion object {
        fun <T> equalsEquality() = object : Equality<T> {
            override fun checkEquality(a: T, b: T) = a == b

            override val name: String = "=="
        }
    }

    constructor(default: T, size: Int, equality: Equality<T> = equalsEquality()) : this(MutableList(size) { null }, default, size, equality)

    val firstEmptyIndex get() =
        elements.indexOfFirst { it == null }
    val lastEmptyIndex get() =
        elements.indexOfLast { it == null }
    val firstNonEmptyIndex get() =
        elements.indexOfFirst { it != null }
    val lastNonEmptyIndex get() =
        elements.indexOfLast { it != null }

    val nonEmpty get() = elements.asSequence().filter { it != null }

    fun isPresent(index: Int) = elements[index] != null
    fun isFull() = elements.none { it == null }

    private fun T.isDefault() = equality.checkEquality(default, this)

    fun updateEmptyAt(index: Int) =
        (elements[index]
            ?.takeIf { it.isDefault() }
            ?.let { elements[index] = null }
            ?.let { true }
            ?: false)

    //region List
    override fun contains(element: T) = elements.contains(element)
    override fun containsAll(elements: Collection<T>) = elements.containsAll(elements)
    override fun get(index: Int) = elements[index] ?: default
    override fun indexOf(element: T) = elements.indexOf(element)
    override fun isEmpty() = elements.all { it == null }
    override fun iterator(): MutableIterator<T> = Iterator(this)
    override fun lastIndexOf(element: T) = elements.lastIndexOf(element)
    override fun listIterator(): MutableListIterator<T> = ListIterator(this)
    override fun listIterator(index: Int): MutableListIterator<T> = ListIterator(this.subList(index, size))
    override fun subList(fromIndex: Int, toIndex: Int): FixedSizeList<T> = FixedSizeList(elements.subList(fromIndex, toIndex), default, toIndex - fromIndex, equality)
    //endregion List

    //region MutableList
    override fun add(element: T): Boolean {
        val index = firstEmptyIndex
        if (index == -1) {
            throw UnsupportedOperationException("Cannot add to full FixedSizeList")
        }
        elements[firstEmptyIndex] = element
        updateEmptyAt(index)
        return true
    }

    override fun add(index: Int, element: T) {
        if (isPresent(index)) {
            throw UnsupportedOperationException("Cannot add to occupied slot ($index) of FixedSizeList")
        }
        elements[index] = element
        updateEmptyAt(index)
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        elements.forEachIndexed { i, element -> add(index + i, element) }
        return true
    }

    override fun addAll(elements: Collection<T>): Boolean {
        elements.forEach { element -> this.add(element) }
        return true
    }

    override fun clear() {
        this.elements.replaceAll { null }
    }

    override fun remove(element: T): Boolean {
        val index = elements.indexOfFirst { it == element }
        if (index == -1) {
            return false
        }
        removeAt(index)
        return true
    }

    override fun removeAll(elements: Collection<T>) =
        elements.fold(false) { removed, element -> removed or remove(element) }

    override fun removeAt(index: Int): T {
        val element = elements[index]
        elements[index] = null
        return element ?: default
    }

    override fun retainAll(elements: Collection<T>) =
        elements.foldIndexed(false) { i, removed, element ->
            if (!this.contains(element)) {
                this.removeAt(i)
                return@foldIndexed true
            } else {
                return@foldIndexed removed
            }
        }

    override fun set(index: Int, element: T): T {
        val oldElement = elements[index]
        elements[index] = element
        updateEmptyAt(index)
        return oldElement ?: default
    }
    //endregion MutableList

    override fun toString() = "FixedSizeList(${elements.indices.map { this[it] }.joinToString(", ")})"
}