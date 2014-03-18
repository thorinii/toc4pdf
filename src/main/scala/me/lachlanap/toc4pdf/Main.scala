package me.lachlanap.toc4pdf

import me.lachlanap.toc4pdf.model._
import me.lachlanap.toc4pdf.proc._

object Main {
  def main(args: Array[String]) = {
    val contents = Contents(List(
      Document("1942", "test2.pdf"),
      Document("1943", "test1.pdf")))
    val config = GeneratorConfig(
      keepOldBookmarks = true,
      forceZoom = ZoomFitWidth)

    new ContentsGenerator(contents, config).process
  }

  private def print(outline: Outline): Unit = {
    outline.topLevelBookmarks.foreach(print(_, 0))
  }

  private def print(bookmark: Bookmark, depth: Int): Unit = {
    bookmark match {
      case ExternalBookmark(name, children, destination) => {
        println(" " * depth + "E " + name + " => " + destination)
      }
      case InternalBookmark(name, children, page, zoom) => {
        println(" " * depth + "I " + name + " => " + page + " (" + zoom.name + ")")
      }
    }

    bookmark.children.foreach { b =>
      print(b, depth + 1)
    }
  }
}