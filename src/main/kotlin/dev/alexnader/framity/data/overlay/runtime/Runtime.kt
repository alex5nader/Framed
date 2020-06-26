package dev.alexnader.framity.data.overlay.runtime

import dev.alexnader.framity.data.overlay.json.Offsetter as JsonOffsetter
import dev.alexnader.framity.data.overlay.json.Offsetters as JsonOffsetters
import dev.alexnader.framity.data.overlay.json.TextureOffsets as JsonTextureOffsets
import dev.alexnader.framity.data.overlay.json.TextureSource as JsonTextureSource
import dev.alexnader.framity.data.overlay.json.OverlayInfo as JsonOverlayInfo

import net.minecraft.block.BlockState
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry

private fun directionFromString(str: String) =
    when (str) {
        "down" -> Direction.DOWN
        "up" -> Direction.UP
        "north" -> Direction.NORTH
        "south" -> Direction.SOUTH
        "east" -> Direction.EAST
        "west" -> Direction.WEST
        else -> error("Invalid direction: $str")
    }

data class OverlayInfo(val textureSource: TextureSource, val coloredLike: ColoredLike? = null, val offsets: TextureOffsets? = null) {
    companion object {
        fun fromJson(jsonOverlayInfo: JsonOverlayInfo) =
            OverlayInfo(
                TextureSource.fromJson(jsonOverlayInfo.textureSource),
                jsonOverlayInfo.coloredLike?.let { ColoredLike.fromJson(it) },
                jsonOverlayInfo.textureOffsets?.let { TextureOffsets.fromJson (it) }
            )
    }
}

sealed class TextureSource {
    companion object {
        fun fromJson(jsonTextureSource: JsonTextureSource) =
            when (jsonTextureSource) {
                is JsonTextureSource.Single -> Single.fromJson(jsonTextureSource)
                is JsonTextureSource.Sided -> Sided.fromJson(jsonTextureSource)
            }
    }

    data class Single(val spriteId: Identifier) : TextureSource() {
        companion object {
            fun fromJson(jsonSingle: JsonTextureSource.Single) =
                Single(Identifier(jsonSingle.sprite))
        }
    }

    data class Sided(val map: Map<Direction, Identifier>) : TextureSource(), Map<Direction, Identifier> by map {
        companion object {
            fun fromJson(jsonSided: JsonTextureSource.Sided) =
                Sided(
                    jsonSided.elements
                        .flatMap { it.sides.map { side -> Pair(
                            directionFromString(side),
                            Identifier(it.value)
                        ) } }
                        .toMap()
                )
        }
    }
}

data class ColoredLike(val colorSource: BlockState) {
    companion object {
        fun fromJson(jsonColoredLike: String) =
            ColoredLike(Registry.BLOCK.get(Identifier(jsonColoredLike)).defaultState)
    }
}

data class TextureOffsets(val map: Map<Direction, Offsetters>) : Map<Direction, Offsetters> by map {
    companion object {
        fun fromJson(jsonTextureOffsets: JsonTextureOffsets): TextureOffsets? {
            return TextureOffsets(
                jsonTextureOffsets.elements
                    .flatMap { it.sides.map { side -> Pair(
                        directionFromString(side),
                        Offsetters.fromJson(it.value) ?: return@fromJson null)
                    } }
                    .toMap()
            )
        }
    }
}

sealed class Offsetters {
    companion object {
        fun fromJson(jsonOffsetters: JsonOffsetters): Offsetters? {
            return if (jsonOffsetters.uOffsetter != null) {
                if (jsonOffsetters.vOffsetter != null) {
                    UV(
                        Offsetter.fromJson(jsonOffsetters.uOffsetter) ?: return null,
                        Offsetter.fromJson(jsonOffsetters.vOffsetter) ?: return null
                    )
                } else {
                    U(Offsetter.fromJson(jsonOffsetters.uOffsetter) ?: return null)
                }
            } else {
                if (jsonOffsetters.vOffsetter != null) {
                    V(Offsetter.fromJson(jsonOffsetters.vOffsetter) ?: return null)
                } else {
                    error("Invalid Offsetters JSON: $jsonOffsetters")
                }
            }
        }
    }

    data class U(val uOffsetter: Offsetter) : Offsetters()
    data class V(val vOffsetter: Offsetter) : Offsetters()
    data class UV(val uOffsetter: Offsetter, val vOffsetter: Offsetter) : Offsetters()
}

sealed class Offsetter {
    companion object {
        fun fromJson(jsonOffsetter: JsonOffsetter) =
            when (jsonOffsetter) {
                is JsonOffsetter.Remap -> Remap.fromJson(jsonOffsetter)
            }
    }

    data class Remap(val map: Map<Float4, Float4>) : Offsetter(), Map<Float4, Float4> by map {
        companion object {
            fun fromJson(jsonRemap: JsonOffsetter.Remap): Remap? {
                return Remap(jsonRemap.elements.map {
                    Pair(
                        Float4.fromList(it.from) ?: return@fromJson null,
                        Float4.fromList(it.to) ?: return@fromJson null
                    )
                }.toMap())
            }
        }
    }
}

data class Float4(val a: Float, val b: Float, val c: Float, val d: Float) {
    companion object {
        fun fromList(list: List<Float>) =
            if (list.size != 4) {
                null
            } else {
                Float4(list[0], list[1], list[2], list[3])
            }
    }
}
