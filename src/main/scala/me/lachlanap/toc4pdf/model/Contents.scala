package me.lachlanap.toc4pdf.model

import java.nio.file.Path

case class Contents(documents: Seq[Document])

case class Document(title: String, file: Path) {
  val relativeFile = file.getName(file.getNameCount-1)
  val filename = file.getName(file.getNameCount-1).toString
}