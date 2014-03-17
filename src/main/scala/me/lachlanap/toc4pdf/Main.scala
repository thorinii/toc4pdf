package me.lachlanap.toc4pdf

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.action.`type`._
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination._;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

object Main {
  def main(args: Array[String]) = {
    val document = PDDocument.load("test2.pdf")

    val outline = new BookmarkParser(document).bookmarks
    print(outline)

    document.close()
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
        println(" " * depth + "I " + name + " (" + zoom.name + ")")
      }
    }

    bookmark.children.foreach { b =>
      print(b, depth + 1)
    }
  }
}