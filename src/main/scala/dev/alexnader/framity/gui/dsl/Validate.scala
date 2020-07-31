package dev.alexnader.framity.gui.dsl

import io.github.cottonmc.cotton.gui.GuiDescription
import io.github.cottonmc.cotton.gui.widget.WPanel

object Validate {
  def validate(panel: WPanel): Validate = new Validate(panel)
}

class Validate(panel: WPanel) {
  def using(desc: GuiDescription): Unit = panel.validate(desc)
}
