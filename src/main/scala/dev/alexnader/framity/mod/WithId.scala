package dev.alexnader.framity.mod

import net.minecraft.util.Identifier

import scala.language.implicitConversions

object WithId {
  implicit class MakeWithId[+A](value: A) {
    def withId(id: Identifier): WithId[A] = WithId(id, value)

    def withId(path: String)(implicit mod: Mod): WithId[A] = WithId(mod.id(path), value)
  }

  implicit def getContained[A](withId: WithId[A]): A = withId.contained
}

case class WithId[+A](id: Identifier, contained: A)
