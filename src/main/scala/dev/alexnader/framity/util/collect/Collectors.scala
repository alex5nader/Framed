package dev.alexnader.framity.util.collect

object Collectors {
  implicit def list[A]: Collector[A, List[A]] = _.toList

  implicit def seq[A]: Collector[A, Seq[A]] = _.toSeq

  implicit def set[A]: Collector[A, Set[A]] = _.toSet

  implicit def either[A, B](implicit seqCollector: Collector[B, Seq[B]] = Collectors.seq[B]): Collector[Either[A, B], Either[A, Seq[B]]] = (iterator: Iterator[Either[A, B]]) =>
    iterator.foldRight(Right(Iterator.empty): Either[A, Iterator[B]]) { case (e, acc) => for (xs <- acc; x <- e) yield Iterator(x) ++ xs }.map(_.collectTo(seqCollector))
}
