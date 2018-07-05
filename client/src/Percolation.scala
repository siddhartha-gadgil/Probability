package probability

import scala.scalajs.js
import scala.util.Random
import mhtml._
import org.scalajs.dom
import org.scalajs.dom.document

import scala.collection.immutable
import scala.xml.{Elem, Node}

case class Percolation(n: Int, m: Int, edges: Set[((Int, Int), (Int, Int))]) {
  val xmax = 400
  val ymax = 400

  val xscale = xmax.toDouble / n
  val yscale = ymax.toDouble / m

  val gridLines =
    (0 to m).map { (i) =>
      <line x1="0" y1={(i * yscale).toInt.toString} x2={(n * xscale).toInt.toString} y2={(i * yscale).toInt.toString} stroke="grey" stroke-dasharray="1 1" xmlns="http://www.w3.org/2000/svg"></line>
    } ++
      ((0 to n).map { (j) =>
        <line x1={(j * xscale).toInt.toString} y1="0" x2={(j * xscale).toInt.toString} y2={(m * yscale).toInt.toString} stroke="grey" stroke-dasharray="1 1" xmlns="http://www.w3.org/2000/svg"></line>
      })

  val edgeLines =
    for {
      ((x1, y1), (x2, y2)) <- edges
    } yield
      <line x1={(x1 * xscale).toInt.toString} y1={(y1 * yscale).toInt.toString} x2={(x2 * xscale).toInt.toString} y2={(y2 * yscale).toInt.toString} stroke="black" stroke-width="1" xmlns="http://www.w3.org/2000/svg"></line>


  def neighbours(i: Int, j: Int) =
    Set(
      (i, j) ->  (i, j + 1),
      (i - 1, j) -> (i, j),
      (i, j) -> (i, j + 1),
      (i, j -1) -> (i, j)
    ).intersect(edges).flatMap{
      case (p, q) => Set(p, q)
    } - (i -> j)

  @annotation.tailrec
  final def findPath(
    source: Set[Vector[(Int, Int)]],
    target: Set[(Int, Int)]
  ) : Option[Vector[(Int, Int)]] = {
    val pathOpt = source.find((p) =>
      target.contains(p.last))
    if (pathOpt.nonEmpty) pathOpt
    else {
        val endPoints = source.map(_.last)
        val adjPoints = endPoints.flatMap{case (i, j) => neighbours(i, j)}
        val support = source.flatMap((p) => p.toSet)
        val newPoints = adjPoints -- support
        if (newPoints.isEmpty) None
        else {
          val newPaths =
            newPoints.flatMap{case (i, j) =>
              source.find((path) => neighbours(i, j).contains(path.last))
            }
          findPath(newPaths, target)
        }
      }
  }

  // val topToBottom =
  //   findPath(
  //     (0 to n).map((i) => Vector(i -> 0)).toSet,
  //     (0 to n).map((i) => (i -> m)).toSet
  //   )
  //
  // val greenLines: Seq[Elem] =
  //   topToBottom.map{
  //     (v) =>
  //     for {
  //   ((x1, y1), (x2, y2)) <- v.zip(v.tail)
  //     } yield
  //       <line x1={(x1 * xscale).toInt.toString} y1={(y1 * yscale).toInt.toString} x2={(x2 * xscale).toInt.toString} y2={(y2 * yscale).toInt.toString} stroke="green" stroke-width="2" xmlns="http://www.w3.org/2000/svg"></line>
  //     }.getOrElse(Seq())

  val allLines: immutable.IndexedSeq[Elem] = gridLines ++ edgeLines.toSeq //++ greenLines

  val view =
      <svg viewBox="0 0 400 400" width="800" height="400" xmlns="http://www.w3.org/2000/svg">
        {allLines}
      </svg>
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
          <p>See if the top and bottom are connected</p>
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
