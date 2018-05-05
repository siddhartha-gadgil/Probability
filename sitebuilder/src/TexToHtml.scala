package probability

import ammonite.ops._

import scala.util.matching._

object TeXToHtml {
  val top =
    """
<html>
<head>

<!-- Latest compiled and minified CSS  for Bootstrap -->
<link rel="stylesheet" href="../css/bootstrap.min.css">

  <!-- mathjax config similar to math.stackexchange -->
  <script type="text/javascript" async
        src="https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.1/MathJax.js?config=TeX-MML-AM_CHTML">
      </script>


  <script type="text/x-mathjax-config">
  MathJax.Hub.Config({
  jax: ["input/TeX", "output/HTML-CSS"],
  tex2jax: {
  inlineMath: [ ['$', '$'] ],
  displayMath: [ ['$$', '$$'], ['\\[', '\\]' ]],
  TeX: { equationNumbers: { autoNumber: "AMS" } },
  processEscapes: true,
  skipTags: ['script', 'noscript', 'style', 'textarea', 'pre', 'code']
  },
  messageStyle: "none",
  "HTML-CSS": { preferredFont: "TeX", availableFonts: ["STIX","TeX"] }
  });
  </script>
</head>
<body>
<div class="container">
<p>&nbsp;</p>

"""

  val foot =
    """
</div>
  </body>
</body>
</html>
"""

  val defReg =
    "(\\\\def|\\\\newcommand|\\\\renewcommand)(\\\\[a-zA-Z0-9]+)([^\n%]+)".r

  val newCommReg = "(\\\\newcommand\\{)(\\\\[a-zA-Z0-9]+)(\\}[^\n%]+)".r

  val renewCommReg = "(\\\\renewcommand\\{)(\\\\[a-zA-Z0-9]+)(\\}[^\n%]+)".r

  def trimLine(l: String): String =
    if (l.startsWith("%")) ""
    else
      """[^\\]%""".r
        .findFirstMatchIn(l)
        .map((m) => m.before.toString + m.group(0).head)
        .getOrElse(l)

  def readTrim(f: Path) = read.lines(f).map(trimLine).mkString("", "\n", "\n")

  val wd = pwd / 'sitebuilder / "resources"

  val preamble = readTrim(wd / "mypreamble_2017.tex")

  val texFile = readTrim(wd / "stat-and-prob.tex")

  val fullText = texFile.replace("\\input{mypreamble_2017}", preamble)

  val Array(header, textPadded) = fullText.split("\\\\begin\\{document\\}")

  val text = textPadded
    .split("""\\maketitle""")
    .last
    .split("""\\begin\{thebibliography\}""")
    .head

  val begReg = """\\begin\{([^\}\{]+|[^\{\}]*\{[^\{\}]*\}[^\{\}]*)\}""".r

  val fullBegReg = """\\begin\{([a-zA-Z0-9\*]*)\}(\[[^\[\]]+\])?(\\label\{[a-zA-Z0-9]+\})?""".r

  val endReg = """\\end\{([^\}\{]+|[^\{\}]*\{[^\{\}]*\}[^\{\}]*)\}""".r

  val converter = new TeXToHtml(header, text)

  val dolReg = "[^\\$]\\$[^\\$]".r

  val doldolReg = "\\$\\$".r

  val begItReg = """\\begin\{itemize\}""".r

  val endItReg = """\\end\{itemize\}""".r

  val begEnReg = """\\begin\{enumerate\}""".r

  val endEnReg = """\\begin\{enumerate\}""".r

  val blankLineReg = "\n([ \t]*\n)".r

  def maxOpt[B: Ordering](v: Vector[B]): Option[B] =
    if (v.isEmpty) None else Some(v.max)

  val thmEnvs = Map(
    "example" -> "Example",
    "question" -> "Question",
    "definition" -> "Definition",
    "remark" -> "Remark",
    "exercise" -> "Exercise",
    "lemma" -> "Lemma",
    "problem" -> "Problem",
    "proposition" -> "Proposition",
    "theorem" -> "Theorem"
  )

  val mathEnvs = Set(
    "align*",
    "equation*",
    "aligned",
    "array",
    "equation",
    "tabular"
  )

  val divEnvs = Set("proof")

  def indices(r: Regex, txt: String): Vector[Int] =
    r.findAllMatchIn(txt).toVector.map(_.start)

  def dollarIndices(txt: String): Vector[Int] = indices(dolReg, txt).map(_ + 1)

  def displayIndices(txt: String): Vector[Int] = indices(doldolReg, txt)

  def itemizeBegs(txt: String): Vector[Int] = indices(begItReg, txt)

  def itemizeEnds(txt: String): Vector[Int] = indices(endItReg, txt)

  def enumerateBegs(txt: String): Vector[Int] = indices(begEnReg, txt)

  def enumerateEnds(txt: String): Vector[Int] = indices(begEnReg, txt)

  def items(txt: String): Vector[Int] = indices("""\\item""".r, txt)

  def firstItem(j: Int, txt: String): Boolean = {
    val lastBeg = (enumerateBegs(txt) ++ itemizeBegs(txt)).filter(_ < j).max
    maxOpt(items(txt).filter(_ < j))
      .map {
        _ < lastBeg
      }
      .getOrElse(true)

  }

  def inMath(j: Int, txt: String): Boolean =
    dollarIndices(txt).filter(_ < j).size % 2 == 1

  def inDisplayMath(j: Int, txt: String): Boolean =
    displayIndices(txt).filter(_ < j).size % 2 == 1

  def replaceItems(txt: String): String = {
    val itemLess =
      """\\item""".r.replaceAllIn(
        txt,
        (m) => if (firstItem(m.start, txt)) "<li>" else "</li>\n<li>")
    itemLess
      .replace("""\begin{itemize}""", "<ul>")
      .replace("""\end{itemize}""", "</ul>")
      .replace("""\begin{enumerate}""", "<ol>")
      .replace("""\end{enumerate}""", "</ol>")
  }

  def replaceBegins(txt: String) =
    begReg.replaceAllIn(
      txt,
      (m) =>
//        if (inMath(m.start, txt) || inDisplayMath(m.start, txt)) m.group(0)
//        else
        if (thmEnvs.keySet.contains(m.group(1))) s"""<div class="${m
          .group(1)}"> <strong>${thmEnvs(m.group(1))}</strong> """
        else if (mathEnvs.contains(m.group(1)))
          Regex.quoteReplacement("$$" + m.group(0))
        else s"""<div class="${m.group(1)}">"""
    )

//  def rplBegins(txt: String, head: String = ""): String = {
//    begReg
//      .findFirstMatchIn(txt)
//      .map { (m) =>
//        val newString =
//          if (thmEnvs.keySet.contains(m.group(1))) s"""<div class="${m
//            .group(1)}"> <strong>${thmEnvs(m.group(1))}</strong> """
//          else if (mathEnvs.contains(m.group(1)))
//            Regex.quoteReplacement("$$" + m.group(0))
//          else if (divEnvs.contains(m.group(1))) s"""<div class="${m.group(1)}">"""
//            else Regex.quoteReplacement(m.group(0))
//        rplBegins(m.after.toString, head + m.before.toString + newString)
//      }
//      .getOrElse(head + txt)
//  }.replace("""\\begin""", """\begin""").replace("""\$""", "$")

  def recRplBegins(txt: String, head: String = "", thmCounter: Int = 0, labels: Map[String, Int] = Map()): (String, Int, Map[String, Int]) = {
    fullBegReg
      .findFirstMatchIn(txt)
      .map { (m) =>
        val title = Option(m.group(2))
        val labelOpt = Option(m.group(3)).map(_.drop("""\label{""".size).dropRight(1))
        val newCounter = if (thmEnvs.keySet.contains(m.group(1))) thmCounter + 1 else thmCounter
        val newLabels =  if (thmEnvs.keySet.contains(m.group(1))) labelOpt.map((l) => labels + (l -> newCounter)).getOrElse(labels) else labels
        val newString =
          if (thmEnvs.keySet.contains(m.group(1))) s"""<div class="${m
            .group(1)}"> <strong>${thmEnvs(m.group(1))} $newCounter ${title.map((s) => "("+s.drop(1).dropRight(1)+")").getOrElse("")}</strong>"""
          else if (mathEnvs.contains(m.group(1)))
            Regex.quoteReplacement("$$" + m.group(0))
          else if (divEnvs.contains(m.group(1))) s"""<div class="${m.group(1)}">"""
          else Regex.quoteReplacement(m.group(0))
        recRplBegins(m.after.toString, head + m.before.toString + newString, newCounter, newLabels)
      }
      .getOrElse((head + txt, thmCounter, labels))
  }

  def rplBegins(txt: String) : String = recRplBegins(txt)._1.replace("""\\begin""", """\begin""").replace("""\$""", "$")


  def replaceEnds(txt: String) =
    endReg.replaceAllIn(
      txt,
      (m) =>
//        if (inMath(m.start, txt) || inDisplayMath(m.start, txt)) {println(s"${m.group(0)} in math environment"); m.group(0)}
//        else
        if (mathEnvs.contains(m.group(1)))
          Regex.quoteReplacement(m.group(0) + "$$")
        else if (thmEnvs.keySet.union(divEnvs).contains(m.group(1))) "</div>"
        else Regex.quoteReplacement(m.group(0))
    )

  def replaceBlanks(txt: String): String =
    blankLineReg.replaceAllIn(
      txt, (m) =>
        if (inDisplayMath(m.start, txt)) m.group(0) else "</p>\n<p class=\"text-justify\">")

  def replaceSec(txt: String) =
    """(\\section\{)([^\}\{]+|[^\{\}]*\{[^\{\}]*\}[^\{\}]*)\}""".r
      .replaceAllIn(txt, (m) => s"""<h2>${m.group(2)}</h2><p class="text-justify">""")

  def replaceSubSec(txt: String) =
    """(\\subsection\{)([^\}\{]+|[^\{\}]*\{[^\{\}]*\}[^\{\}]*)\}""".r
      .replaceAllIn(txt, (m) => s"""<strong>${m.group(2)} .</strong><p class="text-justify">""")

  def replacePara(txt: String) =
    """(\\para\{)([^\}\{]+|[^\{\}]*\{[^\{\}]*\}[^\{\}]*)\}""".r
      .replaceAllIn(
        txt,
        (m) => s"<strong>${Regex.quoteReplacement(m.group(2))}:</strong>")

  def replaceParag(txt: String) =
    """(\\parag\{)([^\}\{]+|[^\{\}]*\{[^\{\}]*\}[^\{\}]*)\}""".r
      .replaceAllIn(
        txt,
        (m) => s"<strong>${Regex.quoteReplacement(m.group(2))}</strong>")

  val bfReg1 = "(\\{\\\\bf )([^\\}]+)\\}".r

  val bfReg2 = "(\\{\\\\bf\\{)([^\\}]+)\\}\\}".r

  def replaceBf(txt: String) = {
    val step = bfReg1.replaceAllIn(
      txt,
      (m) => Regex.quoteReplacement(s"<strong>${m.group(2)}</strong>"))
    bfReg2.replaceAllIn(step, (m) => s"<strong>${m.group(2)}</strong>")
  }

  val emReg = "(\\{\\\\em )([^\\}]+)\\}".r

  def replaceEm(txt: String) =
    emReg.replaceAllIn(txt,
                       (m) => Regex.quoteReplacement(s"<em>${m.group(2)}</em>"))
}

class TeXToHtml(header: String, text: String) {
  import TeXToHtml._

  val defs = defReg.findAllMatchIn(header).toVector

  val newCommands = newCommReg.findAllMatchIn(header).toVector

  val renewCommands = renewCommReg
    .findAllMatchIn(header)
    .toVector ++ renewCommReg
    .findAllMatchIn(header)
    .toVector

  val defSubs =
    (defs.map((m) => m.group(2) -> m.group(3).trim.drop(1).dropRight(1)) ++
      (newCommands ++ renewCommands).map((m) =>
        m.group(2) -> m.group(3).trim.drop(2).dropRight(1))).filterNot((c) =>
      Set("\\I", "\\matrices", "\\para", "\\parag").contains(c._1))

  def defReplace(txt: String) = defSubs.foldLeft[String](txt) {
    case (t, (x, y)) =>
      new Regex(x.replace("\\", "\\\\") + "([^a-zA-Z0-9])")
        .replaceAllIn(t, (m) => Regex.quoteReplacement(y + m.group(1)))
  }

  def recDefReplace(txt: String): String = {
    val next = defReplace(txt)
    if (next == txt) next else recDefReplace(next)
  }

  lazy val defReplaced = recDefReplace(text).replace("""\noindent""", "")



  lazy val baseReplaced =
    replaceEm(
      replaceBf(
        replacePara(replaceParag(
          replaceBlanks(replaceEnds(replaceItems(defReplaced)))))))

  lazy val allReplaced = replaceSec(rplBegins(baseReplaced))

  lazy val newFile = header + """\begin{document}""" + defReplaced

  def replace() = write.over(wd / "repl.tex", newFile)

  lazy val crudeHtml = top + allReplaced + foot

  def html() = write.over(pwd / "docs" / "crude" / "index.html", crudeHtml)
}

object CrudeBuild extends App{
  import TeXToHtml._
  converter.html()
  println(fullBegReg.findAllIn(text))
}

/*
The cases for begin{_} :
Vector(
  "center",
  "example",
  "question",
  "definition",
  "itemize",
  "cases",
  "enumerate",
  "remark",
  "align*",
  "exercise",
  "lemma",
  "problem",
  "proposition",
  "proof",
  "equation*",
  "aligned",
  "figure",
  "theorem",
  "array",
  "equation",
  "tabular",
  "thebibliography"
)

 */
