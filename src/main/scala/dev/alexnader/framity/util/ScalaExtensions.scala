package dev.alexnader.framity.util

object ScalaExtensions {
  implicit class IsInRange(val i: Int) {
    def isIn(range: Range): Boolean = range contains i
  }

  implicit class MinMax(a: Float) {
    def minMax(b: Float): (Float, Float) = {
      if (a < b) {
        (a, b)
      } else {
        (b, a)
      }
    }
  }

  implicit class EitherToOption[+A, +B](either: Either[A, B]) {
    def leftOrNone: Option[A] = either.left.map(Some.apply).left.getOrElse[Option[A]](None)
    def rightOrNone: Option[B] = either.map(Some.apply).getOrElse[Option[B]](None)
  }

  implicit class ToTuple[A](iterable: Iterable[A]) {
    def toTuple4: (A, A, A, A) = {
      val iterator = iterable.iterator
      (iterator.next, iterator.next, iterator.next, iterator.next)
    }
  }

  implicit class Tuple4Ext(tuple: (Float, Float, Float, Float)) {
    def center: Float = {
      val min = tuple._1 min tuple._2 min tuple._3 min tuple._4
      val max = tuple._1 max tuple._2 max tuple._3 max tuple._4
      (min + max) / 2
    }
  }
}
