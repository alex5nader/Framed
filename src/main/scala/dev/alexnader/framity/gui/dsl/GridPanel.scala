package dev.alexnader.framity.gui.dsl

import io.github.cottonmc.cotton.gui.widget.WGridPanel

object GridPanel {
  object gridPanel {
    def ofSize(size: Int): WGridPanel = new WGridPanel(size)
  }
}
