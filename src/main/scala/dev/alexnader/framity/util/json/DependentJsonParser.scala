package dev.alexnader.framity.util.json

import net.minecraft.util.Identifier

trait DependentJsonParser[A] {
  def parseDependencies(context: JsonParseContext): JsonParseResult[Iterable[Identifier]]

  def run(dependencies: Map[Identifier, A], context: JsonParseContext): JsonParseResult[A]
}
