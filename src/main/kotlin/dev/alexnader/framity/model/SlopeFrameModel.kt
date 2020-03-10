package dev.alexnader.framity.model

import arrow.core.Tuple6
import arrow.syntax.function.curried
import arrow.syntax.function.andThen
import dev.alexnader.framity.adapters.buildMesh
import dev.alexnader.framity.adapters.mirrored
import dev.alexnader.framity.util.BlockStateMap
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.StairsBlock
import net.minecraft.block.enums.BlockHalf
import net.minecraft.block.enums.StairShape
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.state.property.Property
import net.minecraft.util.math.Direction
import java.lang.Exception
import java.util.function.Function

/**
 * [BaseFrameModel] subclass for slope models.
 *
 * @param block The block to derive [BlockState]s from.
 * @param properties All properties which alter [Mesh] generation. If a property does not affect [Mesh] generation, it should not be present.
 */
class SlopeFrameModel(
    block: Block,
    properties: List<Property<out Comparable<*>>>,
    sprite: SpriteIdentifier,
    transformerFactory: () -> MeshTransformer,
    defaultState: BlockState,
    spriteMap: Function<SpriteIdentifier, Sprite>
) : BaseFrameModel(sprite, transformerFactory, defaultState, spriteMap) {
    companion object {
        /**
         * Partially applies [part] such that it covers the area from (0, 0) to (1, 1) with [pos] and a color of -1.
         */
        private fun sizeToFull(part: (QuadEmitter, Float, Int, Float, Float, Float, Float) -> QuadEmitter, pos: Float) =
            part.curried() andThen { it(pos)(-1)(0f)(0f)(1f)(1f) }

        /**
         * Curried function which produces geometry-bound triangle function.
         */
        val tri =
            { h1: Float, v1: Float ->
            { h2: Float, v2: Float ->
            { h3: Float, v3: Float ->
            { p: Float ->
            { f: (QuadEmitter, Float, Int, Float, Float, Float, Float, Float, Float) -> QuadEmitter ->
                f.curried() andThen { it(p)(-1)(h1)(v1)(h2)(v2)(h3)(v3) }
            }}}}}

        /**
         * Flips the uvs of a triangle function.
         */
        fun <A> flipUv(orig: (Float, Float) -> (Float, Float) -> (Float, Float) -> A) =
            { h1: Float, v1: Float ->
            { h2: Float, v2: Float ->
            { h3: Float, v3: Float ->
                orig(h3, v3)(h2, v2)(h1, v1)
            }}}

        /**
         * Curried function which produces a `(QuadEmitter) -> QuadEmitter` which
         * emits a non culled quad with the given geometry and direction.
         */
        val nonCulledQuad =
            { dir: Direction ->
            { x1: Float, y1: Float, z1: Float ->
            { x2: Float, y2: Float, z2: Float ->
            { x3: Float, y3: Float, z3: Float ->
            { x4: Float, y4: Float, z4: Float ->
                (QuadEmitter::nonCulledQuad).curried() andThen { it(-1)(dir)(x1)(y1)(z1)(x2)(y2)(z2)(x3)(y3)(z3)(x4)(y4)(z4) }
            }}}}}

        /**
         * Curried function which produces a `(QuadEmitter) -> QuadEmitter` which
         * emits a non culled tri with the given geometry and direction.
         */
        val nonCulledTri =
            { dir: Direction ->
            { x1: Float, y1: Float, z1: Float ->
            { x2: Float, y2: Float, z2: Float ->
            { x3: Float, y3: Float, z3: Float ->
                (QuadEmitter::nonCulledTri).curried() andThen { it(-1)(dir)(x1)(y1)(z1)(x2)(y2)(z2)(x3)(y3)(z3) }
            }}}}

        /**
         * Adapts a geometry function to produce an east side triangle emitter.
         * Produces a `(QuadEmitter) -> QuadEmitter` which emits the triangle.
         */
        private fun eastTri(shape: (Float) -> ((QuadEmitter, Float, Int, Float, Float, Float, Float, Float, Float) -> QuadEmitter) -> (QuadEmitter) -> QuadEmitter) =
            shape(1f)(QuadEmitter::eastTriangle)
        /**
         * Adapts a geometry function to produce a west side triangle emitter.
         * Produces a `(QuadEmitter) -> QuadEmitter` which emits the triangle.
         */
        private fun westTri(shape: (Float) -> ((QuadEmitter, Float, Int, Float, Float, Float, Float, Float, Float) -> QuadEmitter) -> (QuadEmitter) -> QuadEmitter) =
            shape(0f)(QuadEmitter::westTriangle)
        /**
         * Adapts a geometry function to produce a north side triangle emitter.
         * Produces a `(QuadEmitter) -> QuadEmitter` which emits the triangle.
         */
        private fun northTri(shape: (Float) -> ((QuadEmitter, Float, Int, Float, Float, Float, Float, Float, Float) -> QuadEmitter) -> (QuadEmitter) -> QuadEmitter) =
            shape(0f)(QuadEmitter::northTriangle)
        /**
         * Adapts a geometry function to produce a south side triangle emitter.
         * Produces a `(QuadEmitter) -> QuadEmitter` which emits the triangle.
         */
        private fun southTri(shape: (Float) -> ((QuadEmitter, Float, Int, Float, Float, Float, Float, Float, Float) -> QuadEmitter) -> (QuadEmitter) -> QuadEmitter) =
            shape(1f)(QuadEmitter::southTriangle)

        /**
         * Map from [Direction] to one of [northTri], [eastTri], [southTri], and [westTri].
         */
        val dirToTriEmitter = mapOf(
            Direction.NORTH to ::northTri,
            Direction.EAST to ::eastTri,
            Direction.SOUTH to ::southTri,
            Direction.WEST to ::westTri
        )

        /**
         * Map from [Direction] to [Direction.AxisDirection] to [Tuple6] of [Int]
         * (geometry coordinates) for a triangle based on the two keys.
         */
        val halfAndDirToTriVerts = mapOf(
            Direction.DOWN to mapOf(
                Direction.AxisDirection.POSITIVE to Tuple6(0f, 0f, 1f, 1f, 1f, 0f),
                Direction.AxisDirection.NEGATIVE to Tuple6(0f, 0f, 0f, 1f, 1f, 0f)
            ),
            Direction.UP to mapOf(
                Direction.AxisDirection.POSITIVE to Tuple6(0f, 1f, 1f, 1f, 1f, 0f),
                Direction.AxisDirection.NEGATIVE to Tuple6(0f, 0f, 0f, 1f, 1f, 1f)
            )
        )

        /**
         * Map from [Direction] to `(QuadEmitter) -> QuadEmitter` which
         * emits a full sized quad along the direction used as the key.
         */
        val dirToFull = mapOf(
            Direction.DOWN to sizeToFull(QuadEmitter::downRect, 0f),
            Direction.UP to sizeToFull(QuadEmitter::upRect, 1f),
            Direction.NORTH to sizeToFull(QuadEmitter::northRect, 0f),
            Direction.EAST to sizeToFull(QuadEmitter::eastRect, 1f),
            Direction.SOUTH to sizeToFull(QuadEmitter::southRect, 1f),
            Direction.WEST to sizeToFull(QuadEmitter::westRect, 0f)
        )

        /**
         * Map from two [Direction]s to `(QuadEmitter) -> QuadEmitter` which
         * emits a straight slope quad with half = key 1 and facing = key 2.
         */
        val halfAndDirToSlope = mapOf(
            Direction.DOWN to mapOf(
                Direction.NORTH to nonCulledQuad(Direction.NORTH)(0f, 0f, 1f)(1f, 0f, 1f)(1f, 1f, 0f)(0f, 1f, 0f),
                Direction.EAST to nonCulledQuad(Direction.EAST)(0f, 0f, 0f)(0f, 0f, 1f)(1f, 1f, 1f)(1f, 1f, 0f),
                Direction.SOUTH to nonCulledQuad(Direction.SOUTH)(1f, 0f, 0f)(0f, 0f, 0f)(0f, 1f, 1f)(1f, 1f, 1f),
                Direction.WEST to nonCulledQuad(Direction.WEST)(1f, 0f, 1f)(1f, 0f, 0f)(0f, 1f, 0f)(0f, 1f, 1f)
            ),
            Direction.UP to mapOf(
                Direction.NORTH to nonCulledQuad(Direction.NORTH)(0f, 0f, 0f)(1f, 0f, 0f)(1f, 1f, 1f)(0f, 1f, 1f),
                Direction.EAST to nonCulledQuad(Direction.EAST)(1f, 0f, 0f)(1f, 0f, 1f)(0f, 1f, 1f)(0f, 1f, 0f),
                Direction.SOUTH to nonCulledQuad(Direction.SOUTH)(1f, 0f, 1f)(0f, 0f, 1f)(0f, 1f, 0f)(1f, 1f, 0f),
                Direction.WEST to nonCulledQuad(Direction.WEST)(0f, 0f, 1f)(0f, 0f, 0f)(1f, 1f, 0f)(1f, 1f, 1f)
            )
        )

        /**
         * Map from two [Direction]s and [StairShape] to `(QuadEmitter) -> QuadEmitter`
         * which emits a corner slope quad with half = key 1, facing = key 2, and shape = key 3.
         */
        val halfAndDirAndShapeToSlope = mapOf(
            Direction.DOWN to mapOf(
                Direction.NORTH to mapOf(
                    StairShape.INNER_LEFT to nonCulledTri(Direction.NORTH)(1f, 0f, 1f)(1f, 1f, 0f)(0f, 1f, 0f),
                    StairShape.INNER_RIGHT to nonCulledTri(Direction.NORTH)(0f, 0f, 1f)(1f, 1f, 0f)(0f, 1f, 0f),
                    StairShape.OUTER_LEFT to nonCulledTri(Direction.NORTH)(0f, 1f, 0f)(1f, 1f, 0f)(1f, 0f, 1f),
                    StairShape.OUTER_RIGHT to nonCulledTri(Direction.NORTH)(0f, 1f, 0f)(1f, 1f, 0f)(0f, 0f, 1f)
                ),
                Direction.EAST to mapOf(
                    StairShape.INNER_LEFT to nonCulledTri(Direction.EAST)(0f, 0f, 1f)(1f, 1f, 1f)(1f, 1f, 0f),
                    StairShape.INNER_RIGHT to nonCulledTri(Direction.EAST)(0f, 0f, 0f)(1f, 1f, 1f)(1f, 1f, 0f),
                    StairShape.OUTER_LEFT to nonCulledTri(Direction.EAST)(1f, 1f, 0f)(1f, 1f, 1f)(0f, 0f, 1f),
                    StairShape.OUTER_RIGHT to nonCulledTri(Direction.EAST)(1f, 1f, 0f)(1f, 1f, 1f)(0f, 0f, 0f)
                ),
                Direction.SOUTH to mapOf(
                    StairShape.INNER_LEFT to nonCulledTri(Direction.SOUTH)(0f, 0f, 0f)(0f, 1f, 1f)(1f, 1f, 1f),
                    StairShape.INNER_RIGHT to nonCulledTri(Direction.SOUTH)(1f, 0f, 0f)(0f, 1f, 1f)(1f, 1f, 1f),
                    StairShape.OUTER_LEFT to nonCulledTri(Direction.SOUTH)(1f, 1f, 1f)(0f, 1f, 1f)(0f, 0f, 0f),
                    StairShape.OUTER_RIGHT to nonCulledTri(Direction.SOUTH)(1f, 1f, 1f)(0f, 1f, 1f)(1f, 0f, 0f)
                ),
                Direction.WEST to mapOf(
                    StairShape.INNER_LEFT to nonCulledTri(Direction.WEST)(1f, 0f, 0f)(0f, 1f, 0f)(0f, 1f, 1f),
                    StairShape.INNER_RIGHT to nonCulledTri(Direction.WEST)(1f, 0f, 1f)(0f, 1f, 0f)(0f, 1f, 1f),
                    StairShape.OUTER_LEFT to nonCulledTri(Direction.WEST)(0f, 1f, 1f)(0f, 1f, 0f)(1f, 0f, 0f),
                    StairShape.OUTER_RIGHT to nonCulledTri(Direction.WEST)(0f, 1f, 1f)(0f, 1f, 0f)(1f, 0f, 1f)
                )
            ),
            Direction.UP to mapOf(
                Direction.NORTH to mapOf(
                    StairShape.INNER_LEFT to nonCulledTri(Direction.NORTH)(0f, 0f, 0f)(1f, 0f, 0f)(1f, 1f, 1f),
                    StairShape.INNER_RIGHT to nonCulledTri(Direction.NORTH)(0f, 0f, 0f)(1f, 0f, 0f)(0f, 1f, 1f),
                    StairShape.OUTER_LEFT to nonCulledTri(Direction.NORTH)(1f, 1f, 1f)(1f, 0f, 0f)(0f, 0f, 0f),
                    StairShape.OUTER_RIGHT to nonCulledTri(Direction.NORTH)(0f, 1f, 1f)(1f, 0f, 0f)(0f, 0f, 0f)
                ),
                Direction.EAST to mapOf(
                    StairShape.INNER_LEFT to nonCulledTri(Direction.EAST)(1f, 0f, 0f)(1f, 0f, 1f)(0f, 1f, 1f),
                    StairShape.INNER_RIGHT to nonCulledTri(Direction.EAST)(1f, 0f, 0f)(1f, 0f, 1f)(0f, 1f, 0f),
                    StairShape.OUTER_LEFT to nonCulledTri(Direction.EAST)(0f, 1f, 1f)(1f, 0f, 1f)(1f, 0f, 0f),
                    StairShape.OUTER_RIGHT to nonCulledTri(Direction.EAST)(0f, 1f, 0f)(1f, 0f, 1f)(1f, 0f, 0f)
                ),
                Direction.SOUTH to mapOf(
                    StairShape.INNER_LEFT to nonCulledTri(Direction.SOUTH)(1f, 0f, 1f)(0f, 0f, 1f)(0f, 1f, 0f),
                    StairShape.INNER_RIGHT to nonCulledTri(Direction.SOUTH)(1f, 0f, 1f)(0f, 0f, 1f)(1f, 1f, 0f),
                    StairShape.OUTER_LEFT to nonCulledTri(Direction.SOUTH)(0f, 1f, 0f)(0f, 0f, 1f)(1f, 0f, 1f),
                    StairShape.OUTER_RIGHT to nonCulledTri(Direction.SOUTH)(1f, 1f, 0f)(0f, 0f, 1f)(1f, 0f, 1f)
                ),
                Direction.WEST to mapOf(
                    StairShape.INNER_LEFT to nonCulledTri(Direction.WEST)(0f, 0f, 1f)(0f, 0f, 0f)(1f, 1f, 0f),
                    StairShape.INNER_RIGHT to nonCulledTri(Direction.WEST)(0f, 0f, 1f)(0f, 0f, 0f)(1f, 1f, 1f),
                    StairShape.OUTER_LEFT to nonCulledTri(Direction.WEST)(1f, 1f, 0f)(0f, 0f, 0f)(0f, 0f, 1f),
                    StairShape.OUTER_RIGHT to nonCulledTri(Direction.WEST)(1f, 1f, 1f)(0f, 0f, 0f)(0f, 0f, 1f)
                )
            )
        )

        /**
         * Converts [BlockHalf] to [Direction].
         */
        val halfToDir = mapOf(
            BlockHalf.BOTTOM to Direction.DOWN,
            BlockHalf.TOP to Direction.UP
        )
    }

    override val blockStateMap = BlockStateMap<Mesh>()

    init {
        try {
            @Suppress("unchecked_cast")
            properties.forEach { blockStateMap.includeProperty(it as Property<Comparable<Any>>) }

            @Suppress("MapGetWithNotNullAssertionOperator")
            block.stateManager.states.forEach { state ->
                blockStateMap[state] = RENDERER.buildMesh { qe ->
                    val facing = state.get(StairsBlock.FACING)!!
                    val half = halfToDir[state.get(StairsBlock.HALF)]!!
                    when (val shape = state.get(StairsBlock.SHAPE)!!) {
                        StairShape.STRAIGHT -> {

                            dirToFull[facing]!!(qe) // vertical full face
                            dirToFull[half]!!(qe) // horizontal full face

                            val triFormatter = if (facing.direction == Direction.AxisDirection.NEGATIVE) flipUv(tri)
                            else tri

                            // triangle faces
                            val (h1, v1, h2, v2, h3, v3) = halfAndDirToTriVerts[half]!![facing.direction]!!
                            dirToTriEmitter[facing.rotateYClockwise()]!!(triFormatter(h3, v3)(h2, v2)(h1, v1))(qe)
                            dirToTriEmitter[facing.rotateYCounterclockwise()]!!(triFormatter(h1, v1)(h2, v2)(h3, v3))(qe)

                            halfAndDirToSlope[half]!![facing]!!(qe) // slope face
                        }
                        StairShape.INNER_LEFT, StairShape.INNER_RIGHT -> {
                            val dirToTriVerts = halfAndDirToTriVerts[half]!!

                            val (h1, v1, h2, v2, h3, v3) = dirToTriVerts[facing.direction]!!

                            val triFormatter = if (facing.direction == Direction.AxisDirection.NEGATIVE) flipUv(tri)
                            else tri

                            val otherFacing = if (shape == StairShape.INNER_RIGHT) {
                                dirToTriEmitter[facing.rotateYCounterclockwise()]!!(triFormatter(h1, v1)(h2, v2)(h3, v3))(qe)
                                facing.rotateYClockwise()
                            } else {
                                dirToTriEmitter[facing.rotateYClockwise()]!!(triFormatter(h3, v3)(h2, v2)(h1, v1))(qe)
                                facing.rotateYCounterclockwise()
                            }

                            dirToFull[otherFacing]!!(qe)
                            dirToFull[facing]!!(qe)
                            dirToFull[half]!!(qe)

                            halfAndDirAndShapeToSlope[half]!![facing]!![shape]!!(qe)
                            halfAndDirAndShapeToSlope[half]!![otherFacing]!![shape.mirrored]!!(qe)
                        }
                        StairShape.OUTER_LEFT, StairShape.OUTER_RIGHT -> {
                            val dirToTriVerts = halfAndDirToTriVerts[half]!!

                            val (h1, v1, h2, v2, h3, v3) = dirToTriVerts[facing.direction]!!

                            val triFormatter = if (facing.direction == Direction.AxisDirection.NEGATIVE) flipUv(tri)
                            else tri

                            val otherFacing = if (shape == StairShape.OUTER_RIGHT) {
                                dirToTriEmitter[facing.rotateYClockwise()]!!(triFormatter(h3, v3)(h2, v2)(h1, v1))(qe)
                                facing.rotateYClockwise()
                            } else {
                                dirToTriEmitter[facing.rotateYCounterclockwise()]!!(triFormatter(h1, v1)(h2, v2)(h3, v3))(qe)
                                facing.rotateYCounterclockwise()
                            }

                            dirToFull[half]!!(qe)

                            halfAndDirAndShapeToSlope[half.opposite]!![facing.opposite]!![shape]!!(qe)
                            halfAndDirAndShapeToSlope[half.opposite]!![otherFacing.opposite]!![shape.mirrored]!!(qe)
                        }
                    }

                    qe
                }
            }
        } catch (ex: Exception) {
//            ex.printStackTrace()
        }
    }
}