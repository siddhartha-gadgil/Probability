package probability

import MarkovProcess._
import org.scalajs.dom
import org.scalajs.dom._
import org.scalajs.dom.html.{Table, TableRow}
import org.scalajs.dom.svg.{Line, SVG}
import scalatags.JsDom
import scalatags.JsDom.all._

import math._
import ProbDist._

object MarkovView {
  var n: Int = 5

  var p: Double = 0.2

  var markovProcess: FiniteMarkovProcess[Int] = MarkovProcess.sparseRandom(n, p)

  def updateProcess(): Unit = markovProcess = MarkovProcess.sparseRandom(n, p)

  def transProb(i: Int, j: Int): Double = markovProcess.transition(i).prob(j)

  def transRow(i: Int): JsDom.TypedTag[TableRow] = {
    val cols = (1 to n).map { (j) =>
      td(f"${transProb(i, j)}%1.3f")
    }
    tr(cols: _*)
  }

  def transMat: JsDom.TypedTag[Table] = {
    val rows = (1 to n).map(transRow)
    table(`class` := "table table-bordered")(rows: _*)
  }

  var steps: Int = 30

  def init: Int = rnd.nextInt(n) + 1

  var path: Vector[Int] = markovProcess.randomChain(Vector(init), steps)

  def freqs: Int => Int = (j) => frequencies(path).getOrElse(j, 0)

  def propns: Option[Map[Int, Double]] = proportions(path)

  def freqTable: JsDom.TypedTag[Table] = {
    val rows = (1 to n).map { (j) =>
      tr(td(j),
         td(freqs(j)),
         td(f"${propns.map(_.getOrElse(j, 0.0)).getOrElse(0.0)}%1.3f"))
    }
    table(`class` := "table table-striped")(rows: _*)
  }

  def newPath(): Unit = {
    path = markovProcess.randomChain(Vector(init), steps)
  }

  val sc: Int = 600

  val rad: Int = 5

  def vertex(j: Int): (Double, Double) = {
    val theta = (2 * Pi / n) * j
    ((sc / 2 + sc / 4 * cos(theta)).toInt,
     (sc / 2 - sc / 4 * (sin(theta))).toInt)
  }

  def unit(x: Double, y: Double): (Double, Double) =
    (x / sqrt(x * x + y * y), y / sqrt(x * x + y * y))

  def svgView: JsDom.TypedTag[SVG] = {
    import scalatags.JsDom.svgTags._
    import scalatags.JsDom.svgAttrs._

    def drawLine(init: (Double, Double),
                 term: (Double, Double), colour: String = "blue", w: Int = 1): JsDom.TypedTag[Line] = {
      line(x1 := init._1.toInt,
           y1 := init._2.toInt,
           x2 := term._1.toInt,
           y2 := term._2.toInt,
           stroke := colour,
           strokeWidth := w,
           xmlns := "http://www.w3.org/2000/svg")
    }

    def lineArrow(init: (Double, Double),
                  term: (Double, Double)): Vector[JsDom.TypedTag[Line]] = {
      val (xinit, yinit) = init
      val (xt, yterm) = term
      val arrowBase = ((xt * 3 + xinit)/4, (yterm * 3 + yinit)/ 4)
      val (bu, tu) = arrowBase
      val (xu, yu) = unit(xt - xinit, yterm - yinit)
      Vector(
        drawLine(init, term),
        drawLine(arrowBase, (bu - (xu * rad) -  (yu * rad), tu - (yu * rad) + (xu * rad)), "black", 2),
        drawLine(arrowBase, (bu - (xu * rad) + (yu * rad), tu - (yu * rad) - (xu * rad)), "black", 2)
      )
    }

    val lines =
      for {
        i <- 1 to n
        j <- 1 to n
        if transProb(i, j) > 0 && i != j
        l <- lineArrow(vertex(i), vertex(j))
      } yield l

    val vertices =
      for {
        j <- 1 to n
        (x, y) = vertex(j)
      } yield circle(cx := x.toInt, cy := y.toInt, r := rad, fill := "green")

    val content = rect(
      height := sc,
      width := sc,
      fill := "white",
      strokeWidth := 2,
      stroke := "black") +: (vertices.toVector ++ lines.toVector)

    svg(viewBox := { s"0 0 $sc $sc" }, height := "600", width := "80%")(
      content: _*
    )
  }

  def view =
    div(
      h2("Transition Matrix"),
      transMat,
      h2("Frequencies of Numbers"),
      freqTable,
      h2("Sequence of states"),
      path.mkString(" -> "),
      svgView
    )

  def main(): Unit = {
    val positionOpt = Option(dom.document.querySelector("#markov"))

    positionOpt.foreach((node) => node.appendChild(view.render))

    def update(): Unit = {
      positionOpt.foreach { node =>
        node.innerHTML = ""
        node.appendChild(view.render)
      }
    }

  }

}
