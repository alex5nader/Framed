package dev.alexnader.framity.gui.dsl

import net.minecraft.text.{LiteralText, TranslatableText}

object Text {
  object translatable {
    def text(key: String): net.minecraft.text.Text = new TranslatableText(key)
  }

  object literal {
    def text(text: String): net.minecraft.text.Text = new LiteralText(text)
  }
}
