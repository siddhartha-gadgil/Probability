package probability

import MarkovProcess._
import org.scalajs.dom
import org.scalajs.dom._
import org.scalajs.dom.html.{Table, TableRow}
import scalatags.JsDom
import scalatags.JsDom.all._


object MarkovView{
  var n: Int = 5

  var p: Double = 0.2

  var markovProcess: FiniteMarkovProcess[Int] = MarkovProcess.sparseRandom(n, p)

  def updateProcess(): Unit = markovProcess = MarkovProcess.sparseRandom(n, p)

  def transProb(i: Int, j: Int): Double = markovProcess.transition(i).prob(j)

  def transRow(i: Int): JsDom.TypedTag[TableRow] = {
    val cols = (1 to n).map{(j) => td(f"${transProb(i, j)}%1.3f")}
    tr(cols: _*)
  }

  def transMat: JsDom.TypedTag[Table] = {
    val rows = (1 to n).map(transRow)
    table(`class`:= "table table-bordered")(rows : _*)
  }

  var steps: Int = 30

  var init: Int = 1

  var path: Vector[Int] = markovProcess.randomChain(Vector(init), steps)

  def freqs: Int =>Int = (j) => frequencies(path).getOrElse(j, 0)

  def propns: Option[Map[Int, Double]] = proportions(path)


  def freqTable: JsDom.TypedTag[Table] =
    {
      val rows = (1 to n).map{
        (j) => tr(td(j), td(freqs(j)), td(f"${propns.map(_.getOrElse(j, 0.0)).getOrElse(0.0)}%1.3f"))
      }
      table(`class`:="table table-striped")(rows:_*)
    }

  def newPath() : Unit = {
    path = markovProcess.randomChain(Vector(init), steps)
  }

  def view =
    div(
      h2("Transition Matrix"),
      transMat,
      h2("Frequencies of Numbers"),
      freqTable,
      h2("Sequence of states"),
      path.mkString(" -> ")
    )

  def main() : Unit = {
    val positionOpt = Option(dom.document.querySelector("#markov"))

    positionOpt.foreach((node) => node.appendChild(view.render))

    def update() : Unit = {
      positionOpt.foreach{node =>
        node.innerHTML = ""
        node.appendChild(view.render)
//        node.appendChild(div("updated").render)
      }
    }

//    update()
  }


}