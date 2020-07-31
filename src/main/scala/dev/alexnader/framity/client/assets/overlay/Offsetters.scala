package dev.alexnader.framity.client.assets.overlay

import dev.alexnader.framity.util.json.{JsonParseContext, JsonParseResult, JsonParser}

object Offsetters {

  implicit object Parser extends JsonParser[Offsetters] {
    override def apply(context: JsonParseContext): JsonParseResult[Offsetters] = {
      val uContext = context("uOffsetter")
      val vContext = context("vOffsetter")

      (uContext, vContext) match {
        case (Right(uContext), Right(vContext)) => uContext.parse[Offsetter].flatMap(uOffsetter => vContext.parse[Offsetter].map(vOffsetter => UV(uOffsetter, vOffsetter)))
        case (Right(uContext), Left(_)) => uContext.parse[Offsetter].map(U.apply)
        case (Left(_), Right(vContext)) => vContext.parse[Offsetter].map(V.apply)
        case (Left(_), Left(_)) => Left(context.makeFailure("Expected key(s) 'uOffsetter', 'vOffsetter', or both."))
      }
    }
  }

  case class U(uOffsetter: Offsetter) extends Offsetters {
    override def apply(us: (Float, Float, Float, Float), vs: (Float, Float, Float, Float)): ((Float, Float, Float, Float), (Float, Float, Float, Float)) = (uOffsetter(us), vs)
  }

  case class V(vOffsetter: Offsetter) extends Offsetters {
    override def apply(us: (Float, Float, Float, Float), vs: (Float, Float, Float, Float)): ((Float, Float, Float, Float), (Float, Float, Float, Float)) = (us, vOffsetter(vs))
  }

  case class UV(uOffsetter: Offsetter, vOffsetter: Offsetter) extends Offsetters {
    override def apply(us: (Float, Float, Float, Float), vs: (Float, Float, Float, Float)): ((Float, Float, Float, Float), (Float, Float, Float, Float)) = (uOffsetter(us), vOffsetter(vs))
  }

}

sealed abstract class Offsetters {
  def apply(us: (Float, Float, Float, Float), vs: (Float, Float, Float, Float)): ((Float, Float, Float, Float), (Float, Float, Float, Float))
}
