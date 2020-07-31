package dev.alexnader.framity.gui.dsl

import io.github.cottonmc.cotton.gui.widget.{WGridPanel, WWidget}

object Add {
  def add(w: WWidget): Add = new Add(w)
}

class Add(widget: WWidget) {
  class To(gridPanel: WGridPanel) {
    def at(x: Int, y: Int): Unit = {
      gridPanel.add(widget, x, y)
    }

    def at(x: Int, y: Int, width: Int, height: Int): Unit = {
      gridPanel.add(widget, x, y, width, height)
    }
  }

  def to(gridPanel: WGridPanel): To = new To(gridPanel)
}
