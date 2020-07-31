package dev.alexnader.framity.mod

import net.minecraft.util.Identifier

object Mod {
  def fromNamespace(_namespace: String): Mod = new Mod {
    override val namespace: String = _namespace
  }

  object Minecraft extends Mod {
    override val namespace: String = "minecraft"
  }
}

trait Mod {
  val namespace: String

  def id(path: String) = new Identifier(namespace, path)
}
