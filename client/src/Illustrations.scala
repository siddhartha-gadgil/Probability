package probability

import org.scalajs.dom
import org.scalajs.dom._

import scala.scalajs.js.annotation._

@JSExportTopLevel("Illustrations")
object Illustrations{
  @JSExport
  def main() : Unit = {
    FairCoin.main()
    Birthdays.main()
  }
}
