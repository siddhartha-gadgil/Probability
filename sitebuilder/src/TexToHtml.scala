package probability

import ammonite.ops._

import scala.util.matching._

object TeXToHtml{
  val defReg = "(\\\\def)(\\\\[a-zA-Z0-9]+)([^\n%]+)".r

  val preamble = read(pwd / 'sitebuilder / "resources" / "mypreamble_2017.tex")

  val texFile = read(pwd / 'sitebuilder / "resources" / "stat-and-prob.tex")

  val fullText = texFile.replace("\\input{mypreamble_2017}", preamble)

  val Array(header, text) = fullText.split("\\\\begin\\{document\\}")
}

class TeXToHtml(header: String, text: String){
  import TeXToHtml._

  val defs = defReg.findAllMatchIn(header).toVector

  val defSubs = defs.map((m) => m.group(2) -> m.group(3).trim.drop(1).dropRight(1))

  val defReplaced = defSubs.foldLeft[String](text){case (t, (x, y)) =>
    new Regex(x.replace("\\", "\\\\")+"[^a-zA-Z0-9]").replaceAllIn(t, y.replace("\\", "\\\\"))}
}
