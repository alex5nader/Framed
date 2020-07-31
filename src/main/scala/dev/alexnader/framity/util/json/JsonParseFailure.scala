package dev.alexnader.framity.util.json

import com.google.gson

object JsonParseFailure {
  implicit class StringJsonParseFailure(message: String) extends JsonParseFailure {
    override def toException: Exception = new gson.JsonParseException(message)
  }

  implicit class ExceptionJsonParseFailure[E >: Exception](exception: E) extends JsonParseFailure {
    override def toException: Exception = exception.asInstanceOf[Exception]
  }
}

trait JsonParseFailure {
  def toException: Exception
}
