package dev.alexnader.framity.util.collect

trait Collector[A, B] {
  def collect(iterator: Iterator[A]): B
}
