package probability

import scala.scalajs.js
import scala.util.Random
import mhtml._
import org.scalajs.dom
import org.scalajs.dom.document

import scala.collection.immutable
import scala.xml.{Elem, Node}

object Svg{
  def polyLine(points: Seq[(Double, Double)], ymax: Int) = {
    val flipped = points.map{case (x, y) => (x, ymax- y)}
    for {
      ((x1, y1), (x2, y2)) <- flipped.zip(flipped.tail)
    } yield
      <line x1={x1.toInt.toString} y1={y1.toInt.toString} x2={x2.toInt.toString} y2={y2.toInt.toString} stroke="black" stroke-width="1" xmlns="http://www.w3.org/2000/svg"></line>
  }
}
