package probability

import org.scalajs.dom
import org.scalajs.dom._
import scala.scalajs.js

import scala.scalajs.js.annotation._

@JSExportTopLevel("Illustrations")
object Illustrations{
  @JSExport
  def main() : Unit = {
    FairCoin.main()
    Birthdays.main()
    Percolation.main()
    CoinTosses.main()
    BayesCoin.main()
    DependentTosses.main()
    Cantor.main()
    MarkovView.main()
  }
}

import js.Dynamic.global
import scala.xml.Node

object Katex{
  def apply(s: String) = {
    val span = document.createElement("SPAN")
    global.katex.render(s, span)
    span
  }
}
