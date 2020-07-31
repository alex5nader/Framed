package dev.alexnader.framity.gui.dsl

import net.minecraft.text.{LiteralText, Text, TranslatableText}

object Text {
  object translatable {
    def text(key: String): Text = new TranslatableText(key)
  }

  object literal {
    def text(text: String): Text = new LiteralText(text)
  }
}
