package me.lachlanap.toc4pdf

case class Outline(topLevelBookmarks: Seq[Bookmark])

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

sealed abstract class Zoom(val name: String)

case object ZoomFitWidth extends Zoom("Fit Width")
case object ZoomFitHeight extends Zoom("Fit Height")
case object ZoomFitRectangle extends Zoom("Fit Rectangle")
case object ZoomFitPage extends Zoom("Fit Page")
case object ZoomAbsolute extends Zoom("Absolute")
case object ZoomOther extends Zoom("?")