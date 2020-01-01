//package dev.alexnader.framity.blocks
//
//import dev.alexnader.framity.adapters.id
//import dev.alexnader.framity.block_entities.FrameEntity
//import dev.alexnader.framity.mixin.AccessibleBlock
//import net.fabricmc.fabric.api.block.FabricBlockSettings
//import net.minecraft.block.*
//import net.minecraft.block.entity.BlockEntity
//import net.minecraft.block.piston.PistonBehavior
//import net.minecraft.client.item.TooltipContext
//import net.minecraft.entity.Entity
//import net.minecraft.entity.EntityContext
//import net.minecraft.entity.EntityType
//import net.minecraft.entity.LivingEntity
//import net.minecraft.entity.player.PlayerEntity
//import net.minecraft.fluid.Fluid
//import net.minecraft.fluid.FluidState
//import net.minecraft.inventory.Inventory
//import net.minecraft.item.*
//import net.minecraft.loot.context.LootContext
//import net.minecraft.server.world.ServerWorld
//import net.minecraft.sound.BlockSoundGroup
//import net.minecraft.state.StateManager
//import net.minecraft.tag.Tag
//import net.minecraft.text.Text
//import net.minecraft.util.*
//import net.minecraft.util.hit.BlockHitResult
//import net.minecraft.util.math.BlockPos
//import net.minecraft.util.math.Direction
//import net.minecraft.util.math.Vec3d
//import net.minecraft.util.shape.VoxelShape
//import net.minecraft.util.shape.VoxelShapes
//import net.minecraft.world.BlockView
//import net.minecraft.world.IWorld
//import net.minecraft.world.World
//import net.minecraft.world.WorldView
//import net.minecraft.world.explosion.Explosion
//import java.util.*
//
//@Suppress("DEPRECATION")
//class Frame(private val shape: Block, private val frameEntityConstructor: (BlockView?) -> FrameEntity) : Block(
//    FabricBlockSettings.of(Material.WOOD).hardness(0.33f).sounds(BlockSoundGroup.WOOD).nonOpaque().build()
//), BlockEntityProvider {
//    override fun isTranslucent(state: BlockState?, view: BlockView?, pos: BlockPos?) = true
//    override fun isSimpleFullBlock(state: BlockState?, view: BlockView?, pos: BlockPos?) = false
//    override fun isSideInvisible(state: BlockState, neighbor: BlockState, facing: Direction?): Boolean {
//        return neighbor.block == this || super.isSideInvisible(state, neighbor, facing)
//    }
//    override fun hasDynamicBounds() = true
//
//    override fun createBlockEntity(view: BlockView?) = frameEntityConstructor(view)
//
//    override fun onBlockRemoved(
//        state: BlockState?, world: World?, pos: BlockPos?, newState: BlockState?, moved: Boolean
//    ) {
//        if (state?.block != newState?.block) {
//            val blockEntity = world?.getBlockEntity(pos)
//
//            if (blockEntity is Inventory) {
//                ItemScatterer.spawn(world, pos, blockEntity)
//                world.updateHorizontalAdjacent(pos, this)
//            }
//
//            super.onBlockRemoved(state, world, pos, newState, moved)
//        }
//    }
//
//    override fun onUse(
//        state: BlockState?, world: World?, pos: BlockPos?, player: PlayerEntity?, hand: Hand?, hit: BlockHitResult?
//    ): ActionResult {
//        if (world?.isClient == true) return ActionResult.CONSUME
//
//        val frameEntity = world?.getBlockEntity(pos) as FrameEntity
//
//        val playerStack = player?.getStackInHand(hand)!!
//
//        return if (!playerStack.isEmpty) {
//            if (playerStack.item !is BlockItem || playerStack.item.id == frameEntity.item.id) {
//                return ActionResult.CONSUME
//            }
//
//            val playerBlock = (playerStack.item as BlockItem).block
//
//            if (playerBlock is Frame) {
//                return ActionResult.CONSUME
//            }
//
//            val outlineShape = playerBlock.getOutlineShape(
//                playerBlock.defaultState, world, pos, EntityContext.absent()
//            )
//
//            if (VoxelShapes.fullCube().boundingBox != outlineShape.boundingBox) {
//                return ActionResult.CONSUME
//            }
//
//            if (!frameEntity.stack.isEmpty) {
//                if (!player.isCreative) {
//                    player.inventory.offerOrDrop(world, frameEntity.stack)
//                }
//            } else {
//                frameEntity.disguised = true
//            }
//
//            val newStackForFrame = playerStack.copy()
//            newStackForFrame.count = 1
//
//            if (!player.isCreative) {
//                playerStack.count -= 1
//            }
//
//            frameEntity.containedState = playerBlock.getPlacementState(ItemPlacementContext(ItemUsageContext(player, hand, hit)))!!
//            frameEntity.stack = newStackForFrame
//
//            ActionResult.SUCCESS
//        } else if (player.isSneaking && !frameEntity.stack.isEmpty) {
//
//            val stackFromBlock = frameEntity.takeInvStack(0, 1)
//            frameEntity.containedState = null
//            frameEntity.disguised = false
//
//            if (!player.isCreative) {
//                player.inventory.offerOrDrop(world, stackFromBlock)
//            }
//
//            ActionResult.SUCCESS
//        } else {
//            ActionResult.CONSUME
//        }.also {
//            if (it == ActionResult.SUCCESS) {
//                frameEntity.sync()
//            }
//        }
//    }
//
//    // ===================================
//    // All below methods delegate to shape
//    // ===================================
//
//    override fun canBucketPlace(state: BlockState?, fluid: Fluid?) = shape.canBucketPlace(state, fluid)
//
//    override fun hasComparatorOutput(state: BlockState?) = shape.hasComparatorOutput(state)
//
//    override fun getMaterial(state: BlockState?): Material = shape.getMaterial(state)
//
//    override fun onBlockBreakStart(state: BlockState?, world: World?, pos: BlockPos?, player: PlayerEntity?) =
//        shape.onBlockBreakStart(state, world, pos, player)
//
//    override fun onBreak(world: World?, pos: BlockPos?, state: BlockState?, player: PlayerEntity?) =
//        shape.onBreak(world, pos, state, player)
//
//    override fun canMobSpawnInside() = shape.canMobSpawnInside()
//
//    override fun onStacksDropped(state: BlockState?, world: World?, pos: BlockPos?, stack: ItemStack?) =
//        shape.onStacksDropped(state, world, pos, stack)
//
//    override fun getPickStack(world: BlockView?, pos: BlockPos?, state: BlockState?): ItemStack =
//        shape.getPickStack(world, pos, state)
//
//    override fun getSoundGroup(state: BlockState?): BlockSoundGroup = shape.getSoundGroup(state)
//
//    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) =
//        (shape as AccessibleBlock).appendProperties(builder)
//
//    override fun dropExperience(world: World?, pos: BlockPos?, size: Int) =
//        (shape as AccessibleBlock).dropExperience(world, pos, size)
//
//    override fun hasInWallOverlay(state: BlockState?, view: BlockView?, pos: BlockPos?) =
//        shape.hasInWallOverlay(state, view, pos)
//
//    override fun onEntityLand(world: BlockView?, entity: Entity?) = shape.onEntityLand(world, entity)
//
//    override fun onEntityCollision(state: BlockState?, world: World?, pos: BlockPos?, entity: Entity?) =
//        shape.onEntityCollision(state, world, pos, entity)
//
//    override fun getStateManager(): StateManager<Block, BlockState> = shape.stateManager
//
//    override fun neighborUpdate(
//        state: BlockState?, world: World?, pos: BlockPos?, block: Block?, neighborPos: BlockPos?, moved: Boolean
//    ) = shape.neighborUpdate(state, world, pos, block, neighborPos, moved)
//
//    override fun getComparatorOutput(state: BlockState?, world: World?, pos: BlockPos?) =
//        shape.getComparatorOutput(state, world, pos)
//
//    override fun getOffsetPos(state: BlockState?, view: BlockView?, blockPos: BlockPos?): Vec3d =
//        shape.getOffsetPos(state, view, blockPos)
//
//    override fun getAmbientOcclusionLightLevel(state: BlockState?, view: BlockView?, pos: BlockPos?) =
//        shape.getAmbientOcclusionLightLevel(state, view, pos)
//
//    override fun getHardness(state: BlockState?, world: BlockView?, pos: BlockPos?) =
//        shape.getHardness(state, world, pos)
//
//    override fun canSuffocate(state: BlockState?, view: BlockView?, pos: BlockPos?) =
//        shape.canSuffocate(state, view, pos)
//
//    override fun getVelocityMultiplier() = shape.velocityMultiplier
//
//    override fun shouldDropItemsOnExplosion(explosion: Explosion?) = shape.shouldDropItemsOnExplosion(explosion)
//
//    override fun getDroppedStacks(state: BlockState?, builder: LootContext.Builder?): MutableList<ItemStack> =
//        shape.getDroppedStacks(state, builder)
//
//    override fun canReplace(state: BlockState?, ctx: ItemPlacementContext?) = shape.canReplace(state, ctx)
//
//    override fun updateNeighborStates(state: BlockState?, world: IWorld?, pos: BlockPos?, flags: Int) =
//        shape.updateNeighborStates(state, world, pos, flags)
//
//    override fun getDropTableId(): Identifier = shape.dropTableId
//
//    override fun calcBlockBreakingDelta(
//        state: BlockState?, player: PlayerEntity?, world: BlockView?, pos: BlockPos?
//    ) = shape.calcBlockBreakingDelta(state, player, world, pos)
//
//    override fun rainTick(world: World?, pos: BlockPos?) = shape.rainTick(world, pos)
//
//    override fun onBroken(world: IWorld?, pos: BlockPos?, state: BlockState?) = shape.onBroken(world, pos, state)
//
//    override fun createContainerProvider(
//        state: BlockState?, world: World?, pos: BlockPos?
//    ) = shape.createContainerProvider(state, world, pos)
//
//    override fun getFluidState(state: BlockState?): FluidState = shape.getFluidState(state)
//
//    override fun hasRandomTicks(state: BlockState?) = shape.hasRandomTicks(state)
//
//    override fun getRenderingSeed(state: BlockState?, pos: BlockPos?) = shape.getRenderingSeed(state, pos)
//
//    override fun afterBreak(
//        world: World?,
//        player: PlayerEntity?,
//        pos: BlockPos?,
//        state: BlockState?,
//        blockEntity: BlockEntity?,
//        stack: ItemStack?
//    ) = shape.afterBreak(world, player, pos, state, blockEntity, stack)
//
//    override fun getMapColor(state: BlockState?, view: BlockView?, pos: BlockPos?): MaterialColor = shape.getMapColor(state, view, pos)
//
//    override fun rotate(state: BlockState?, rotation: BlockRotation?): BlockState = shape.rotate(state, rotation)
//
//    override fun canPlaceAtSide(
//        world: BlockState?, view: BlockView?, pos: BlockPos?, env: BlockPlacementEnvironment?
//    ) = shape.canPlaceAtSide(world, view, pos, env)
//
//    override fun getOutlineShape(
//        state: BlockState?, view: BlockView?, pos: BlockPos?, ePos: EntityContext?
//    ): VoxelShape = shape.getOutlineShape(state, view, pos, ePos)
//
//    override fun getRayTraceShape(state: BlockState?, view: BlockView?, pos: BlockPos?): VoxelShape =
//        shape.getRayTraceShape(state, view, pos)
//
//    override fun getPlacementState(ctx: ItemPlacementContext?) = shape.getPlacementState(ctx)
//
//    override fun asItem(): Item = shape.asItem()
//
//    override fun getCollisionShape(
//        state: BlockState?, view: BlockView?, pos: BlockPos?, ePos: EntityContext?
//    ): VoxelShape = shape.getCollisionShape(state, view, pos, ePos)
//
//    override fun mirror(state: BlockState?, mirror: BlockMirror?): BlockState = shape.mirror(state, mirror)
//
//    override fun getStrongRedstonePower(state: BlockState?, view: BlockView?, pos: BlockPos?, facing: Direction?) =
//        shape.getStrongRedstonePower(state, view, pos, facing)
//
//    override fun onBlockAction(state: BlockState?, world: World?, pos: BlockPos?, type: Int, data: Int) =
//        shape.onBlockAction(state, world, pos, type, data)
//
//    override fun getStateForNeighborUpdate(
//        state: BlockState?,
//        facing: Direction?,
//        neighborState: BlockState?,
//        world: IWorld?,
//        pos: BlockPos?,
//        neighborPos: BlockPos?
//    ): BlockState = shape.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos)
//
//    override fun getJumpVelocityMultiplier() = shape.jumpVelocityMultiplier
//
//    override fun getPistonBehavior(state: BlockState?): PistonBehavior = shape.getPistonBehavior(state)
//
//    override fun matches(tag: Tag<Block>?) = shape.matches(tag)
//
//    override fun allowsSpawning(state: BlockState?, view: BlockView?, pos: BlockPos?, type: EntityType<*>?) =
//        shape.allowsSpawning(state, view, pos, type)
//
//    override fun getBlastResistance() = shape.blastResistance
//
//    override fun scheduledTick(state: BlockState?, world: ServerWorld?, pos: BlockPos?, random: Random?) =
//        shape.scheduledTick(state, world, pos, random)
//
//    override fun emitsRedstonePower(state: BlockState?) = shape.emitsRedstonePower(state)
//
//    override fun toString() = shape.toString()
//
//    override fun getCullingShape(state: BlockState?, view: BlockView?, pos: BlockPos?): VoxelShape =
//        shape.getCullingShape(state, view, pos)
//
//    override fun getOpacity(state: BlockState?, view: BlockView?, pos: BlockPos?) = shape.getOpacity(state, view, pos)
//
//    override fun randomTick(state: BlockState?, world: ServerWorld?, pos: BlockPos?, random: Random?) =
//        shape.randomTick(state, world, pos, random)
//
//    override fun hasEmissiveLighting(state: BlockState?) = shape.hasEmissiveLighting(state)
//
//    override fun getOffsetType(): OffsetType = shape.offsetType
//
//    override fun onPlaced(
//        world: World?, pos: BlockPos?, state: BlockState?, placer: LivingEntity?, itemStack: ItemStack?
//    ) = shape.onPlaced(world, pos, state, placer, itemStack)
//
//    override fun canPlaceAt(state: BlockState?, world: WorldView?, pos: BlockPos?) = shape.canPlaceAt(state, world, pos)
//
//    override fun getName(): Text = shape.name
//
//    override fun hasSidedTransparency(state: BlockState?) = shape.hasSidedTransparency(state)
//
//    override fun buildTooltip(
//        stack: ItemStack?, view: BlockView?, tooltip: MutableList<Text>?, options: TooltipContext?
//    ) = shape.buildTooltip(stack, view, tooltip, options)
//
//    override fun getTickRate(worldView: WorldView?) = shape.getTickRate(worldView)
//
//    override fun getWeakRedstonePower(state: BlockState?, view: BlockView?, pos: BlockPos?, facing: Direction?) =
//        shape.getWeakRedstonePower(state, view, pos, facing)
//
//    override fun getTranslationKey(): String = shape.translationKey
//
//    override fun randomDisplayTick(state: BlockState?, world: World?, pos: BlockPos?, random: Random?) =
//        shape.randomDisplayTick(state, world, pos, random)
//
//    override fun onProjectileHit(world: World?, state: BlockState?, hitResult: BlockHitResult?, entity: Entity?) =
//        shape.onProjectileHit(world, state, hitResult, entity)
//
//    override fun isAir(state: BlockState?) = shape.isAir(state)
//
//    override fun getRenderType(state: BlockState?): BlockRenderType = shape.getRenderType(state)
//
//    override fun onDestroyedByExplosion(world: World?, pos: BlockPos?, explosion: Explosion?) =
//        shape.onDestroyedByExplosion(world, pos, explosion)
//
//    override fun onLandedUpon(world: World?, pos: BlockPos?, entity: Entity?, distance: Float) =
//        shape.onLandedUpon(world, pos, entity, distance)
//
//    override fun shouldPostProcess(state: BlockState?, view: BlockView?, pos: BlockPos?) =
//        shape.shouldPostProcess(state, view, pos)
//
//    override fun getLuminance(state: BlockState?) = shape.getLuminance(state)
//
//    override fun addStacksForDisplay(group: ItemGroup?, list: DefaultedList<ItemStack>?) =
//        shape.addStacksForDisplay(group, list)
//
//    override fun getSlipperiness() = shape.slipperiness
//
//    override fun onSteppedOn(world: World?, pos: BlockPos?, entity: Entity?) = shape.onSteppedOn(world, pos, entity)
//
//    override fun method_9517(state: BlockState?, world: IWorld?, pos: BlockPos?, flags: Int) =
//        shape.method_9517(state, world, pos, flags)
//
//    override fun hasBlockEntity() = shape.hasBlockEntity()
//}