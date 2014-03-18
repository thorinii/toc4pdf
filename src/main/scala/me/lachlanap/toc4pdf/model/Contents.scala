package me.lachlanap.toc4pdf.model

case class Contents(documents: Seq[Document])

case class Document(title: String,
                    file: String) {
  lazy val output = file.substring(0, file.indexOf(".pdf")) + "-out.pdf"
}