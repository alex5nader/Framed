package dev.alexnader.framity.util

import net.minecraft.util.Identifier

import scala.language.implicitConversions

object WithId {
  implicit class MakeWithId[+A](value: A) {
    def withId(id: Identifier): WithId[A] = WithId(id, value)
  }

  implicit def getContained[A](withId: WithId[A]): A = withId.contained
}

case class WithId[+A](id: Identifier, contained: A)
