package dev.alexnader.framity.util

object UnConsTuple {
  implicit def unCons3[A, B, C](t: ((A, B), C)): (A, B, C) = (t._1._1, t._1._2, t._2)

  implicit def unCons4[A, B, C, D](t: (((A, B), C), D)): (A, B, C, D) = (t._1._1._1, t._1._1._2, t._1._2, t._2)
}
