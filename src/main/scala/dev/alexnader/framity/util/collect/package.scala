package dev.alexnader.framity.util

package object collect {
  implicit class CollectIterable[A](iterable: Iterable[A]) {
    def collectTo[B](implicit collector: Collector[A, B]): B = iterable.iterator.collectTo[B]
  }

  implicit class CollectIterator[A](iterator: Iterator[A]) {
    def collectTo[B](implicit collector: Collector[A, B]): B = collector.collect(iterator)
  }

  object test {
    def test(): Unit = {
      implicit val listIntCollector: Collector[Int, List[Int]] = Collectors.list[Int]

      val x = Set(1, 2, 3)
      val y = x.iterator.collectTo[List[Int]]
    }
  }
}
