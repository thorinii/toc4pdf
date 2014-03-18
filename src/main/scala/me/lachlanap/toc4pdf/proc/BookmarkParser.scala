package me.lachlanap.toc4pdf.proc

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.interactive.action.`type`._
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination._
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem

import me.lachlanap.toc4pdf.model._

class BookmarkParser(document: PDDocument) {
  def bookmarks: Outline = {
    val outline = document.getDocumentCatalog().getDocumentOutline()
    Outline(outlineItemsAsStream(outline.getFirstChild).map { item => processItem(item) })
  }

  private def outlineItemsAsStream(item: PDOutlineItem): Stream[PDOutlineItem] = {
    if (item == null)
      Stream.empty
    else
      item #:: outlineItemsAsStream(item.getNextSibling)
  }

  private def processItem(item: PDOutlineItem, depth: String = ""): Bookmark = {
    val children = outlineItemsAsStream(item.getFirstChild).map(processItem(_))

    item.getAction match {
      case actionLaunch: PDActionLaunch =>
        ExternalBookmark(item.getTitle, children, actionLaunch.getFile.getFile)
      case actionGoto: PDActionGoTo =>
        InternalBookmark(item.getTitle, children, pageNumber(actionGoto), zoom(actionGoto))
    }
  }

  private def pageNumber(actionGoto: PDActionGoTo): Int = {
    actionGoto.getDestination match {
      case pageDestination: PDPageDestination => pageNumber(pageDestination)
      case _                                  => 0
    }
  }

  private def pageNumber(pageDestination: PDPageDestination) =
    if (pageDestination.getPageNumber < 0)
      pageDestination.findPageNumber
    else
      pageDestination.getPageNumber

  private def zoom(actionGoto: PDActionGoTo): Zoom = {
    actionGoto.getDestination match {
      case pageDestination: PDPageDestination => zoom(pageDestination)
      case _                                  => ZoomFitWidth
    }
  }

  private def zoom(destination: PDPageDestination) = destination match {
    case _: PDPageFitWidthDestination     => ZoomFitWidth
    case _: PDPageFitHeightDestination    => ZoomFitHeight
    case _: PDPageFitRectangleDestination => ZoomFitRectangle
    case _: PDPageFitDestination          => ZoomFitPage
    case _: PDPageXYZDestination          => ZoomAbsolute
    case _                                => ZoomOther
  }
}
