package me.lachlanap.toc4pdf

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.action.`type`._
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination._;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

object Main {
  def main(args: Array[String]) = {
    val contents = Contents(List(
      Document("1942", "test2.pdf"),
      Document("1943", "test1.pdf")))
    process(contents)
  }

  private def process(contents: Contents) = {
    contents.documents.foreach { thisDoc =>
      val document = PDDocument.load(thisDoc.file)
      val oldOutline = new BookmarkParser(document).bookmarks
      //print(oldOutline)

      val newOutline = Outline(contents.documents.map { aDoc =>
        if (aDoc == thisDoc) {
          InternalBookmark(aDoc.title, oldOutline.topLevelBookmarks, 1, ZoomFitWidth)
        } else
          ExternalBookmark(aDoc.title, List(), aDoc.file)
      })

      new BookmarkWriter(newOutline).write(document)
      document.save(thisDoc.file + ".out.pdf")
      document.close
    }
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