package dev.alexnader.framity.gui.dsl

import io.github.cottonmc.cotton.gui.widget.WLabel
import io.github.cottonmc.cotton.gui.widget.data.{HorizontalAlignment, VerticalAlignment}
import net.minecraft.text.Text

object Label {
  def centered(label: WLabel): WLabel = {
    label.setHorizontalAlignment(HorizontalAlignment.CENTER)
    label.setVerticalAlignment(VerticalAlignment.CENTER)
    label
  }

  object label {
    def containing(text: Text): WLabel = new WLabel(text)
  }
}
