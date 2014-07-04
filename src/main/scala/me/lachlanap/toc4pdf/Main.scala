package me.lachlanap.toc4pdf

import me.lachlanap.toc4pdf.ui.MainFrame
import me.lachlanap.toc4pdf.ui.UIHelpers

object Main {
  def main(args: Array[String]) = {
    UIHelpers.setLaF
    val mainFrame = new MainFrame
    mainFrame.show
  }
}