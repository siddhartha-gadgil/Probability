package probability

import MarkovProcess._
import org.scalajs.dom
import org.scalajs.dom._
import org.scalajs.dom.html.{Div, Input, Table, TableRow}
import org.scalajs.dom.svg.{Line, SVG}
import scalatags.JsDom
import scalatags.JsDom.all._

import math._
import ProbDist._

object MarkovView {
  var n: Int = 5

  var prob: Double = 0.2

  var intervalId: Int = 0

  val logDiv: Div = div().render

  def log(s: String): Unit = logDiv.appendChild(p(s).render)

  var markovProcess: FiniteMarkovProcess[Int] =
    MarkovProcess.sparseRandom(n, prob)

  def updateProcess(): Unit =
    markovProcess = MarkovProcess.sparseRandom(n, prob)

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

  val matrixBox: Input = input(`type` := "button",
                               value := "New Transition Matrix",
                               `class` := "input-btn btn btn-primary").render

  val pathBox: Input = input(`type` := "button",
                             value := "New Path",
                             `class` := "input-btn btn btn-primary").render

  val statesBox: Input = input(`type` := "text", value := n, size := "2").render

  val probBox: Input =
    input(`type` := "text", value := f"$prob%1.3f", size := "4").render

  val speedBox: Input =
    input(`type` := "text", value := "2", size := "2").render

  var speed: Int = 2

//  var steps: Int = 120

  var counter = 0

  def init: Int = rnd.nextInt(n) + 1

  var pth: Stream[Int] = markovProcess.getStream(init)

  def pthHead: Vector[Int] = pth.take(counter + 1).toVector

  def freqs: Int => Int = (j) => frequencies(pthHead).getOrElse(j, 0)

  def propns: Option[Map[Int, Double]] = proportions(pthHead)

  def freqTable: JsDom.TypedTag[Table] = {
    val rows = (1 to n).map { (j) =>
      tr(td(j),
         td(freqs(j)),
         td(f"${propns.map(_.getOrElse(j, 0.0)).getOrElse(0.0)}%1.3f"))
    }
    table(`class` := "table table-striped")(rows: _*)
  }

  def newPath(): Unit = {
    pth = markovProcess.getStream(init)
    counter = 0
  }

  val sc: Int = 600

  val rad: Int = 5

  def vertex(j: Int): (Double, Double) = {
    val theta = (2 * Pi / n) * (j - 1)
    ((sc / 2 + sc / 4 * cos(theta)).toInt,
     (sc / 2 - sc / 4 * (sin(theta))).toInt)
  }

  def unit(x: Double, y: Double): (Double, Double) =
    (x / sqrt(x * x + y * y), y / sqrt(x * x + y * y))

  def svgView: JsDom.TypedTag[SVG] = {
    import scalatags.JsDom.svgTags._
    import scalatags.JsDom.svgAttrs._

    def drawLine(init: (Double, Double),
                 term: (Double, Double),
                 colour: String = "blue",
                 w: Int = 1): JsDom.TypedTag[Line] = {
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
      val arrowBase = ((xt * 3 + xinit) / 4, (yterm * 3 + yinit) / 4)
      val (bu, tu) = arrowBase
      val (xu, yu) = unit(xt - xinit, yterm - yinit)
      Vector(
        drawLine(init, term),
        drawLine(arrowBase,
                 (bu - (xu * rad) - (yu * rad), tu - (yu * rad) + (xu * rad)),
                 "black",
                 2),
        drawLine(arrowBase,
                 (bu - (xu * rad) + (yu * rad), tu - (yu * rad) - (xu * rad)),
                 "black",
                 2)
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

    val active = {
      val v = pth(counter)
      val (x, y) = vertex(v)
//      log(s"counter: $counter")
//      log(s"v: $v")
//      log(s"x : $x, y: $y")
      circle(cx := x.toInt, cy := y.toInt, r := rad * 2, fill := "red")
    }

    val content = rect(
      height := sc,
      width := sc,
      fill := "white",
      strokeWidth := 2,
      stroke := "black") +: (vertices.toVector ++ lines.toVector) :+ active

    svg(viewBox := { s"0 0 $sc $sc" }, height := "600", width := "80%")(
      content: _*
    )
  }

  def view =
    div(
      div(`class` := "row")(
        div(`class` := "col-md-6")(h3("Transition Matrix"),
                                   transMat,
                                   matrixBox),
        div(`class` := "col-md-6")(h3("Frequencies and Proportions"),
                                   freqTable)),
      div(`class` := "row")(h3("Sequence of states"),
                            div(svgView),
                            div(pthHead.mkString(" -> ")),
                            pathBox),
      logDiv
    )

  val dynamicView: Div = div(view).render

  val fullView = div(
    h2("Finite state Markov Process"),
    ul(
      li(span("Number of States: "), statesBox),
      li(span("Probability of transition between a pair of vertices: "),
         probBox),
      li(span("Steps per second: "), speedBox)
    ),
    dynamicView
  )

  def main(): Unit = {
    val positionOpt = Option(dom.document.querySelector("#markov"))

    positionOpt.foreach((node) => node.appendChild(fullView.render))

    def update(): Unit = {
      dynamicView.innerHTML = ""
      dynamicView.appendChild(view.render)
    }

    matrixBox.onclick = (_) => {
      updateProcess()
      newPath()
      update()
    }

    pathBox.onclick = (_) => {
      newPath()
      update()
    }

    statesBox.onchange = (_) => {
      n = statesBox.value.toInt
      updateProcess()
      newPath()
      update()
    }

    probBox.onchange = (_) => {
      prob = probBox.value.toDouble
      updateProcess()
      newPath()
      update()
    }

    def animate(): Unit = {
      intervalId = dom.window.setInterval(
        () => {
          counter = (counter + 1)
//          log(s"counter: $counter")
          update()
        },
        1000 / speed
      )
    }

    animate()

    speedBox.onchange = (_) => {
      speed = speedBox.value.toInt
      dom.window.clearTimeout(intervalId)
      animate()
      update()
    }

  }

}
