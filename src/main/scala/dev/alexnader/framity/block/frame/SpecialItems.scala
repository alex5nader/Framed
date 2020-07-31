package dev.alexnader.framity.block.frame

import java.{lang => jl}

import dev.alexnader.framity.block_entity.FrameEntity
import net.minecraft.item.Items
import net.minecraft.state.property.{BooleanProperty, Properties}
import net.minecraft.world.World

object SpecialItems {

  case class SpecialItem(offset: Int, property: BooleanProperty) {
    def onAdd(world: World, frameEntity: FrameEntity): Unit = {
      world.setBlockState(frameEntity.getPos, world.getBlockState(frameEntity.getPos).`with`[jl.Boolean, jl.Boolean](property, true))
    }

    def onRemove(world: World, frameEntity: FrameEntity): Unit = {
      world.setBlockState(frameEntity.getPos, world.getBlockState(frameEntity.getPos).`with`[jl.Boolean, jl.Boolean](property, false))
    }
  }

  val map = Map(
    Items.GLOWSTONE_DUST -> SpecialItem(0, Properties.LIT),
    Items.REDSTONE -> SpecialItem(1, FrameBlock.Properties.HasRedstone)
  )
}
