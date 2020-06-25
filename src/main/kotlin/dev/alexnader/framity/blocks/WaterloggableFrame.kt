//package dev.alexnader.framity.blocks
//
//import net.minecraft.block.Block
//import net.minecraft.block.BlockState
//import net.minecraft.block.Waterloggable
//import net.minecraft.fluid.FluidState
//import net.minecraft.fluid.Fluids
//import net.minecraft.item.ItemPlacementContext
//import net.minecraft.state.StateManager
//import net.minecraft.state.property.Properties
//import net.minecraft.util.math.BlockPos
//import net.minecraft.util.math.Direction
//import net.minecraft.world.IWorld
//
/////**
//// * [BaseFrame] subclass for all waterloggable frames.
//// */
////abstract class WaterloggableFrame : BaseFrame(), Waterloggable {
////    init {
////        this.defaultState = this.defaultState.with(Properties.WATERLOGGED, false)
////    }
////
////    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
////        super.appendProperties(builder)
////        builder?.add(Properties.WATERLOGGED)
////    }
////
////    override fun getFluidState(state: BlockState?): FluidState =
////        @Suppress("deprecation")
////        if (state?.get(Properties.WATERLOGGED) == true)
////            Fluids.WATER.getStill(false)
////        else
////            super.getFluidState(state)
////
////    override fun getPlacementState(ctx: ItemPlacementContext?) = super.getPlacementState(ctx)
////        ?.with(Properties.WATERLOGGED, ctx?.world?.getFluidState(ctx.blockPos)?.fluid == Fluids.WATER)
////
////    override fun getStateForNeighborUpdate(
////        state: BlockState?,
////        facing: Direction?,
////        neighborState: BlockState?,
////        world: IWorld?,
////        pos: BlockPos?,
////        neighborPos: BlockPos?
////    ): BlockState? {
////        if (state?.get(Properties.WATERLOGGED) == true) {
////            world?.fluidTickScheduler?.schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world))
////        }
////        @Suppress("deprecation")
////        return super.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos)
////    }
////}