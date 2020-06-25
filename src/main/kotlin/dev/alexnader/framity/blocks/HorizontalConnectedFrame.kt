package dev.alexnader.framity.blocks
//
//import dev.alexnader.framity.adapters.mask
//import dev.alexnader.framity.adapters.plus
//import dev.alexnader.framity.adapters.rotated
//import dev.alexnader.framity.util.enumMapOf
//import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
//import net.minecraft.block.BlockPlacementEnvironment
//import net.minecraft.block.BlockState
//import net.minecraft.block.HorizontalConnectingBlock
//import net.minecraft.entity.EntityContext
//import net.minecraft.state.property.BooleanProperty
//import net.minecraft.state.property.Properties
//import net.minecraft.util.BlockMirror
//import net.minecraft.util.BlockRotation
//import net.minecraft.util.math.BlockPos
//import net.minecraft.util.math.Direction
//import net.minecraft.util.shape.VoxelShape
//import net.minecraft.util.shape.VoxelShapes
//import net.minecraft.world.BlockView
//
/////**
//// * [WaterloggableFrame] subclass for all frames which can
//// * be connected horizontally (i.e., wall, fence, etc).
//// */
////abstract class HorizontalConnectedFrame(
////    centerGeometry: VoxelShape,
////    northGeometry: VoxelShape,
////    centerCollision: VoxelShape,
////    northCollision: VoxelShape
////) : WaterloggableFrame() {
////
////    companion object {
////        /**
////         * Maps a horizontal direction to its corresponding [HorizontalConnectingBlock] property.
////         */
////        @JvmStatic
////        protected val FACING_PROPERTIES: Map<Direction, BooleanProperty> = enumMapOf(
////            Pair(Direction.NORTH, HorizontalConnectingBlock.NORTH),
////            Pair(Direction.EAST, HorizontalConnectingBlock.EAST),
////            Pair(Direction.SOUTH, HorizontalConnectingBlock.SOUTH),
////            Pair(Direction.WEST, HorizontalConnectingBlock.WEST)
////        )
////
////        /**
////         * Creates an array of voxel shapes with indices
////         * based on combinations of [Direction.mask].
////         */
////        private fun createShapes(center: VoxelShape, north: VoxelShape): Array<VoxelShape> {
////            val east = north.rotated(-90f)
////            val south = east.rotated(-90f)
////            val west = south.rotated(-90f)
////            val northEast = north + east
////            val southWest = south + west
////
////            return arrayOf(
////                VoxelShapes.empty(),
////                south,
////                west,
////                southWest,
////                north,
////                south + north,
////                west + north,
////                southWest + north,
////                east,
////                south + east,
////                west + east,
////                southWest + east,
////                northEast,
////                south + northEast,
////                west + northEast,
////                southWest + northEast
////            ).map(center::plus).toTypedArray()
////        }
////    }
////
////    /**
////     * Lazily filled map from [BlockState] to [Int].
////     */
////    private val shapeIndexCache = Object2IntOpenHashMap<BlockState>()
////
////    /**
////     * Geometry shapes for this frame.
////     */
////    private val geometryShapes: Array<VoxelShape>
////    /**
////     * Collision shapes for this frame.
////     */
////    private val collisionShapes: Array<VoxelShape>
////
////    init {
////        this.geometryShapes = createShapes(centerGeometry, northGeometry)
////        this.collisionShapes = createShapes(centerCollision, northCollision)
////    }
////
////    override fun getOutlineShape(state: BlockState?, view: BlockView?, pos: BlockPos?, ePos: EntityContext?) =
////        this.geometryShapes[getShapeIndex(state)]
////
////    override fun getCollisionShape(state: BlockState?, view: BlockView?, pos: BlockPos?, ePos: EntityContext?) =
////        this.collisionShapes[getShapeIndex(state)]
////
////    private fun getShapeIndex(state: BlockState?): Int = this.shapeIndexCache.computeIntIfAbsent(state) {
////        var i = 0
////
////        if (it.get(Properties.NORTH))
////            i = i or Direction.NORTH.mask
////
////        if (it.get(Properties.EAST))
////            i = i or Direction.EAST.mask
////
////        if (it.get(Properties.SOUTH))
////            i = i or Direction.SOUTH.mask
////
////        if (it.get(Properties.WEST))
////            i = i or Direction.WEST.mask
////
////        i
////    }
////
////    override fun rotate(state: BlockState?, rotation: BlockRotation?) = when (rotation) {
////        BlockRotation.CLOCKWISE_90 -> state
////            ?.with(HorizontalConnectingBlock.NORTH, state.get(HorizontalConnectingBlock.WEST))
////            ?.with(HorizontalConnectingBlock.EAST, state.get(HorizontalConnectingBlock.NORTH))
////            ?.with(HorizontalConnectingBlock.SOUTH, state.get(HorizontalConnectingBlock.EAST))
////            ?.with(HorizontalConnectingBlock.WEST, state.get(HorizontalConnectingBlock.SOUTH))
////        BlockRotation.CLOCKWISE_180 -> state
////            ?.with(HorizontalConnectingBlock.NORTH, state.get(HorizontalConnectingBlock.SOUTH))
////            ?.with(HorizontalConnectingBlock.EAST, state.get(HorizontalConnectingBlock.WEST))
////            ?.with(HorizontalConnectingBlock.SOUTH, state.get(HorizontalConnectingBlock.NORTH))
////            ?.with(HorizontalConnectingBlock.WEST, state.get(HorizontalConnectingBlock.EAST))
////        BlockRotation.COUNTERCLOCKWISE_90 -> state
////            ?.with(HorizontalConnectingBlock.NORTH, state.get(HorizontalConnectingBlock.EAST))
////            ?.with(HorizontalConnectingBlock.EAST, state.get(HorizontalConnectingBlock.SOUTH))
////            ?.with(HorizontalConnectingBlock.SOUTH, state.get(HorizontalConnectingBlock.WEST))
////            ?.with(HorizontalConnectingBlock.WEST, state.get(HorizontalConnectingBlock.NORTH))
////        else -> state
////    }
////
////    override fun mirror(state: BlockState?, mirror: BlockMirror?) = when (mirror) {
////        BlockMirror.LEFT_RIGHT -> state
////            ?.with(HorizontalConnectingBlock.NORTH, state.get(HorizontalConnectingBlock.SOUTH))
////            ?.with(HorizontalConnectingBlock.SOUTH, state.get(HorizontalConnectingBlock.NORTH))
////        BlockMirror.FRONT_BACK -> state
////            ?.with(HorizontalConnectingBlock.EAST, state.get(HorizontalConnectingBlock.WEST))
////            ?.with(HorizontalConnectingBlock.WEST, state.get(HorizontalConnectingBlock.EAST))
////        else -> @Suppress("deprecation") super.mirror(state, mirror)
////    }
////
////    override fun canPlaceAtSide(world: BlockState?, view: BlockView?, pos: BlockPos?, env: BlockPlacementEnvironment?) =
////        false
////}
