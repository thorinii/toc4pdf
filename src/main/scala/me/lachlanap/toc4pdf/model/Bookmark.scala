package me.lachlanap.toc4pdf.model

case class Outline(topLevelBookmarks: Seq[Bookmark]) {
  def map(func: (Bookmark, Seq[Bookmark]) => Bookmark): Outline =
    Outline(multiMap(topLevelBookmarks, func))

  private def multiMap(bookmarks: Seq[Bookmark], func: (Bookmark, Seq[Bookmark]) => Bookmark): Seq[Bookmark] =
    bookmarks.map(singleMap(_, func))

  private def singleMap(bookmark: Bookmark, func: (Bookmark, Seq[Bookmark]) => Bookmark): Bookmark =
    func(bookmark, multiMap(bookmark.children, func))
}

sealed abstract class Bookmark(val name: String,
                               val children: Seq[Bookmark])

case class ExternalBookmark(override val name: String,
                            override val children: Seq[Bookmark],
                            destination: String)
  extends Bookmark(name, children)

case class InternalBookmark(override val name: String,
                            override val children: Seq[Bookmark],
                            page: Int,
                            zoom: Zoom)
  extends Bookmark(name, children)

sealed abstract class Zoom(val name: String) {
  final override def toString = name
}

object Zoom {
  val AutoZooms = List(ZoomFitWidth, ZoomFitHeight, ZoomFitPage)
}

case object ZoomFitWidth extends Zoom("Fit Width")
case object ZoomFitHeight extends Zoom("Fit Height")
case object ZoomFitRectangle extends Zoom("Fit Rectangle")
case object ZoomFitPage extends Zoom("Fit Page")
case object ZoomAbsolute extends Zoom("Absolute")
case object ZoomOther extends Zoom("?")