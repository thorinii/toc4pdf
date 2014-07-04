package me.lachlanap.toc4pdf.proc

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.nio.file.{ Path, Paths , Files}

import org.apache.pdfbox.pdfparser.PDFParser
import org.apache.pdfbox.pdmodel.PDDocument

import me.lachlanap.toc4pdf.model._

case class GeneratorConfig(
  keepOldBookmarks: Boolean = true,
  forceZoom: Zoom = ZoomFitWidth,
  author: String = "",
  output: Path = Paths.get(""))

trait GeneratorListener {
  def starting(count: Int)
  def reading(name: String, from: String)
  def writing(name: String, to: String)
  def finished(name: String)
  def finishedAll
}

class ContentsGenerator(contents: Contents, config: GeneratorConfig, listener: GeneratorListener) {
  def process = {
    listener.starting(contents.documents.size)
    contents.documents.foreach(processDocument)
    listener.finishedAll
  }

  private def processDocument(doc: Document) = {
    listener.reading(doc.title, doc.filename)

    val pdDoc = loadPdf(doc)
    try {
      val oldOutline = new BookmarkParser(pdDoc).bookmarks

      val newOutline = Outline(contents.documents.map(makeBookmark(doc, _, oldOutline)))
      new BookmarkWriter(newOutline).write(pdDoc)

      pdDoc.getDocumentInformation.setTitle(doc.title)
      pdDoc.getDocumentInformation.setAuthor(config.author)
      Option(pdDoc.getDocumentCatalog.getViewerPreferences).foreach { prefs =>
        println(prefs.fitWindow)
        println(prefs.getViewArea)
      }

      listener.writing(doc.title, doc.filename)
      savePdf(doc, pdDoc)
    } finally {
      pdDoc.close
    }

    listener.finished(doc.title)
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
        otherDoc.filename)
  }

  private def forceZoom(outline: Outline): Outline = outline.map { (bookmark, children) =>
    bookmark match {
      case InternalBookmark(name, _, page, zoom) =>
        InternalBookmark(name, children, page, config.forceZoom)
      case ExternalBookmark(name, _, destination) =>
        ExternalBookmark(name, children, destination)
    }
  }

  private def outputFor(doc: Document) = config.output.resolve(doc.relativeFile)

  private def loadPdf(doc: Document) = {
    val stream = new BufferedInputStream(Files.newInputStream(doc.file), 64 * 1024)

    val parser = new PDFParser(stream)
    parser.parse()
    parser.getPDDocument
  }

  private def savePdf(doc: Document, pdDoc: PDDocument) = {
    val stream = new BufferedOutputStream(Files.newOutputStream(outputFor(doc)), 64 * 1024)

    pdDoc.save(stream)
  }
}
