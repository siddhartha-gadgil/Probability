package probability

import scala.scalajs.js
import scala.util.Random
import mhtml._
import org.scalajs.dom
import org.scalajs.dom.document

import scala.collection.immutable
import scala.xml.{Elem, Node}

case class Percolation(n: Int, m: Int, edges: Set[((Int, Int), (Int, Int))]) {
  lazy val xmax = (xscale * n).toInt // 400
  lazy val ymax = (yscale * m).toInt// 400

  lazy val xscale = 40.0 // xmax.toDouble / n
  lazy val yscale = 40.0 //ymax.toDouble / m

  lazy val gridLines: immutable.IndexedSeq[Elem] =
    (0 to m).map { (i) =>
      <line x1="0" y1={(i * yscale).toInt.toString} x2={(n * xscale).toInt.toString} y2={(i * yscale).toInt.toString} stroke="grey" stroke-dasharray="1 1" xmlns="http://www.w3.org/2000/svg"></line>
    } ++
      ((0 to n).map { (j) =>
        <line x1={(j * xscale).toInt.toString} y1="0" x2={(j * xscale).toInt.toString} y2={(m * yscale).toInt.toString} stroke="grey" stroke-dasharray="1 1" xmlns="http://www.w3.org/2000/svg"></line>
      })

  lazy val edgeLines: Set[Elem] =
    for {
      ((x1, y1), (x2, y2)) <- edges
    } yield
      <line x1={(x1 * xscale).toInt.toString} y1={(y1 * yscale).toInt.toString} x2={(x2 * xscale).toInt.toString} y2={(y2 * yscale).toInt.toString} stroke="black" stroke-width="1" xmlns="http://www.w3.org/2000/svg"></line>


  def adjacent(i: Int, j: Int): Set[((Int, Int), (Int, Int))] =
    Set(
      (i, j) ->  (i + 1, j),
      (i - 1, j) -> (i, j),
      (i, j) -> (i, j + 1),
      (i, j -1) -> (i, j)
    )

 def dualEdge(edge: ((Int, Int), (Int, Int))): ((Int, Int), (Int, Int)) = edge match {
   case ((a, b), (c, d)) if c == a + 1 && d == b => ((a, b), (a, b + 1))
   case ((a, b), (c, d)) if c == a && d == b + 1 => ((a - 1, b + 1), (a, b + 1))
   case _ => throw  new IllegalArgumentException(s"dual of a strange edge $edge")
 }

  def neighbours(i: Int, j: Int): Set[(Int, Int)] =
    adjacent(i, j).intersect(edges).flatMap{
      case (p, q) => Set(p, q)
    } - (i -> j)

 def dualNeighbours(i: Int, j: Int): Set[(Int, Int)] = {
   require(0 <= j && j < m, s"dual neighbours of ($i, $j)")
   (adjacent(i, j).filterNot{(e) => edges.contains(dualEdge(e))}.flatMap{
     case (p, q) => Set(p, q)
   } - (i -> j)).filter{
     case (x, y) => 0 <= y && y < m && 0 <= x && x <= n + 1
   }
 }

  @annotation.tailrec
  final def findPath(
    source: Set[Vector[(Int, Int)]],
    target: Set[(Int, Int)],
    nbrs: (Int, Int) => Set[(Int, Int)]
  ) : Option[Vector[(Int, Int)]] = {
    val pathOpt = source.find((p) =>
      target.contains(p.last))
    if (pathOpt.nonEmpty) pathOpt
    else {
        val endPoints = source.map(_.last)
        val adjPoints = endPoints.flatMap{case (i, j) => nbrs(i, j)}
        val support = source.flatMap((p) => p.toSet)
        assert(support.intersect(target).isEmpty, s"point in support but not endpoint")
        val newPoints = adjPoints -- support
        if (newPoints.isEmpty) None else {
          val newPaths =
            newPoints.flatMap{case (i, j) =>
              source.find((path) => nbrs(i, j).contains(path.last)).map((v) => v :+ (i -> j))
            }
          assert(newPaths.map(_.last) == newPoints, s"Missing link: source: $source \nNew points: $newPoints")
          findPath(newPaths, target, nbrs)
        }
      }
  }

  val top = (0 to n).map((i) => Vector(i -> 0)).toSet

  val bottom = (0 to n).map((i) => i -> m).toSet

  val topToBottom =
    findPath(
      top,
      bottom,
      neighbours
    )

  val left = (0 until m).map((i) => Vector(0 -> i)).toSet
  val right = (0 until m).map(i => (n + 1) -> i).toSet

  lazy val leftToRight = findPath(left, right, dualNeighbours)

  lazy val blueLines: Seq[Elem] =
    topToBottom.map{
      (v) =>
      for {
    ((x1, y1), (x2, y2)) <- v.zip(v.tail)
      } yield
        <line x1={(x1 * xscale).toInt.toString} y1={(y1 * yscale).toInt.toString} x2={(x2 * xscale).toInt.toString} y2={(y2 * yscale).toInt.toString} stroke="blue" stroke-width="2" xmlns="http://www.w3.org/2000/svg"></line>
      }.getOrElse(Seq())

 val redLines: Seq[Elem] =
   leftToRight.map{
     (v) =>
       for {
         ((x1, y1), (x2, y2)) <- v.zip(v.tail)
       } yield
         <line x1={((x1 - 0.5) * xscale).toInt.toString} y1={((y1 + 0.5) * yscale).toInt.toString} x2={((x2 - 0.5) * xscale).toInt.toString} y2={((y2 + 0.5) * yscale).toInt.toString} stroke="red" stroke-width="2" xmlns="http://www.w3.org/2000/svg"></line>
   }.getOrElse(Seq())

  lazy val allLines: immutable.IndexedSeq[Elem] = gridLines ++ edgeLines.toSeq ++ blueLines ++ redLines

  lazy val connected =
    if (topToBottom.isEmpty) <p>No path from top to bottom: nothing crosses the red line.</p>
    else <p>The blue path connects the top to the bottom</p>

  lazy val view =
      <div>
      <svg viewBox={s"0 0 $xmax $ymax"} width="800" height="500" xmlns="http://www.w3.org/2000/svg">
        {allLines}
      </svg>
      <p></p>
      {connected}
      </div>
}

object Percolation {
  val rnd = new Random()

  def random(n: Int, m: Int): Percolation = {
    val horizontals =
      for {
        i <- 0 until n
        j <- 0 to m
        if rnd.nextDouble() > 0.5
      } yield ((i, j), (i + 1, j))

    val verticals =
      for {
        i <- 0 to n
        j <- 0 until m
        if rnd.nextDouble() > 0.5
      } yield ((i, j), (i, j + 1))

    val edges = horizontals.toSet.union(verticals.toSet)
    Percolation(n, m, edges)
  }

  val nV: Var[Int] = Var(10)

  val mV: Var[Int] = Var(10)

  val percolation: Var[Percolation] = Var(random(10, 10))

  val percView = percolation.map(_.view)

  def main() : Unit = {
    val percDiv: Node =
      <div class="panel panel-primary">
        <div class="panel-heading">Percolation</div>
        <div class="panel-body">
          <p><strong>Event:</strong> the top and bottom are connected</p>
          <div>
            <label>Number of columns (n):</label>
            <input type="number" min="0" max ="100" value ="10" oninput={
          (e: js.Dynamic) =>
            nV := e.target.value.asInstanceOf[String].toInt
          }/>
            <label>Number of rows (m)</label>
            <input type="number" min="0" max ="100" value ="10" oninput={
          (e: js.Dynamic) =>
            mV := e.target.value.asInstanceOf[String].toInt
          }/>
          {nV.zip(mV).map{ case (n, m) =>
          <button class="btn btn-default" type="button" onclick={
          () => percolation := random(n, m)}>
            New random percolation
          </button>
        }}
        <p></p>
            {percView}

          </div>

        </div>
      </div>

    val positionOpt = Option(dom.document.querySelector("#percolation"))
    positionOpt.foreach { (position) =>
      val div = document.createElement("div")
      position.appendChild(div)
      mount(div, percDiv)
    }

  }




}
