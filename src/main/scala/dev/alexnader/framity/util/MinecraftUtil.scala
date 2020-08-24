package dev.alexnader.framity.util

import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.{CompoundTag, Tag}
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry

object MinecraftUtil {
  implicit class CompoundTagExt(myTag: CompoundTag) {
    def apply(key: String): Tag = myTag.get(key)
    def update(key: String, tag: Tag): Unit = myTag.put(key, tag)
  }

  implicit class IterableInventory(inventory: Inventory) extends Iterable[Option[ItemStack]] {
    override def iterator: Iterator[Option[ItemStack]] = new Iterator[Option[ItemStack]] {
      private var nextIndex = 0

      override def hasNext: Boolean = nextIndex < inventory.size()

      override def next(): Option[ItemStack] = {
        val stack = Some(inventory.getStack(nextIndex)).filter(!_.isEmpty)
        nextIndex += 1
        stack
      }
    }
  }

  implicit class Vec3dExt(`this`: Vec3d) {
    def -(that: Vec3d): Vec3d = `this`.subtract(that)
  }

  implicit class RegistryExt[A](registry: Registry[A]) {
    def register(value: WithId[A]): A = {
      Registry.register(registry, value.id, value)
    }

    def register(values: WithId[A]*): Seq[A] = {
      values map { value =>
        Registry.register(registry, value.id, value.contained)
      }
    }
  }
}
