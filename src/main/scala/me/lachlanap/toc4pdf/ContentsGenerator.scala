package me.lachlanap.toc4pdf
import org.apache.pdfbox.pdmodel.PDDocument

case class GeneratorConfig(
  keepOldBookmarks: Boolean = true,
  forceZoom: Zoom = ZoomFitWidth,
  author: String = "")

class ContentsGenerator(contents: Contents, config: GeneratorConfig) {
  def process = {
    contents.documents.foreach(processDocument _)
  }

  private def processDocument(doc: Document) = {
    val pdDoc = PDDocument.load(doc.file)
    val oldOutline = new BookmarkParser(pdDoc).bookmarks

    val newOutline = Outline(contents.documents.map(makeBookmark(doc, _, oldOutline)))
    new BookmarkWriter(newOutline).write(pdDoc)

    pdDoc.getDocumentInformation.setAuthor(config.author)
    Option(pdDoc.getDocumentCatalog.getViewerPreferences).foreach { prefs =>
      println(prefs.fitWindow)
      println(prefs.getViewArea)
    }
    pdDoc.save(doc.output)
    pdDoc.close
  }

  private def makeBookmark(doc: Document, otherDoc: Document, oldOutline: Outline) = {
    if (otherDoc == doc)
      InternalBookmark(
        doc.title,
        if (config.keepOldBookmarks)
          forceZoom(oldOutline).topLevelBookmarks
        else
          List(),
        1,
        config.forceZoom)
    else
      ExternalBookmark(
        otherDoc.title,
        List(),
        otherDoc.output)
  }

  private def forceZoom(outline: Outline): Outline = outline.map { (bookmark, children) =>
    bookmark match {
      case InternalBookmark(name, _, page, zoom) =>
        InternalBookmark(name, children, page, config.forceZoom)
      case ExternalBookmark(name, _, destination) =>
        ExternalBookmark(name, children, destination)
    }
  }
}
