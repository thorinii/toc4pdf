package me.lachlanap.toc4pdf

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.common.filespecification.PDSimpleFileSpecification
import org.apache.pdfbox.pdmodel.interactive.action.`type`.PDActionLaunch
import org.apache.pdfbox.pdmodel.interactive.action.`type`.PDActionGoTo
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination._
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem

class BookmarkWriter(outline: Outline) {
  def write(document: PDDocument) = {
    val pdOutline = new PDDocumentOutline

    outline.topLevelBookmarks.foreach { b => pdOutline.appendChild(pdBookmark(b)) }
    pdOutline.openNode

    document.getDocumentCatalog.setDocumentOutline(pdOutline)
  }

  private def pdBookmark(bookmark: Bookmark): PDOutlineItem = {
    val item = new PDOutlineItem
    item.setTitle(bookmark.name)

    item.setAction(pdAction(bookmark))

    bookmark.children.foreach { b => item.appendChild(pdBookmark(b)) }

    item.openNode
    item
  }

  private def pdAction(bookmark: Bookmark) = bookmark match {
    case external: ExternalBookmark =>
      val action = new PDActionLaunch
      val fileSpec = new PDSimpleFileSpecification
      fileSpec.setFile(external.destination)
      action.setFile(fileSpec)
      action
    case internal: InternalBookmark =>
      val action = new PDActionGoTo
      action.setDestination(pdDestination(internal))
      action
  }

  private def pdDestination(bookmark: InternalBookmark) = {
    val destination = bookmark.zoom match {
      case ZoomFitWidth     => new PDPageFitWidthDestination
      case ZoomFitHeight    => new PDPageFitHeightDestination
      case ZoomFitRectangle => new PDPageFitRectangleDestination
      case ZoomFitPage      => new PDPageFitDestination
      case ZoomAbsolute     => new PDPageXYZDestination
      case _                => new PDPageFitWidthDestination
    }

    destination.setPageNumber(bookmark.page - 1)
    destination
  }
}
