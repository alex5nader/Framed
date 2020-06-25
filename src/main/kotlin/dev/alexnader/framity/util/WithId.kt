package dev.alexnader.framity.util

/**
 * Wrapper type for objects that stores an id.
 */
data class WithId<T>(val id: String, val value: T)
