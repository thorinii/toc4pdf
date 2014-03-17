package me.lachlanap.toc4pdf

case class Contents(documents: Seq[Document]) {

}

case class Document(title: String, file: String)