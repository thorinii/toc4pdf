package me.lachlanap.toc4pdf.ui

import java.awt.GridBagConstraints

object Anchor extends Enumeration {
  import GridBagConstraints._

  type Anchor = Value
  val Center = Value(CENTER)
  val West = Value(WEST)
  val East = Value(EAST)
}

object Fill extends Enumeration {
  import GridBagConstraints._

  type Fill = Value
  val None = Value(NONE)
  val Horizontal = Value(HORIZONTAL)
  val Vertical = Value(VERTICAL)
  val Both = Value(BOTH)
}

case class Insets(top: Int, left: Int, bottom: Int, right: Int)

object Insets {
  def apply(same: Int) = new Insets(same, same, same, same)
}

import Anchor._
import Fill._
case class GBC(x: Int, y: Int,
               width: Int = 1, height: Int = 1,
               weightX: Double = 0, weightY: Double = 0,
               anchor: Anchor = Center, fill: Fill = None,
               insets: Insets = Insets(5),
               padX: Int = 0, padY: Int = 0) {

  import GridBagConstraints._
  def toGBC: GridBagConstraints = {
    new GridBagConstraints(
      x, y,
      width, height,
      weightX, weightY,
      anchor.id, fill.id,
      new java.awt.Insets(insets.top, insets.left, insets.bottom, insets.right),
      padX, padY)
  }
}