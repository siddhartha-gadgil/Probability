package probability

import ammonite.ops._

import scala.util.matching._

object TeXToHtml {
  val top =
    """
<html>
<head>
<meta charset="utf-8">
   <meta http-equiv="X-UA-Compatible" content="IE=edge">
   <meta name="viewport" content="width=device-width, initial-scale=1">

<title> Probability Models and Stastics</title>
<link rel="icon" href="../IIScLogo.jpg">

<!-- Latest compiled and minified CSS  for Bootstrap -->
<link rel="stylesheet" href="../css/bootstrap.min.css">

<style type="text/css">
   body { padding-top: 60px; }
   .section {padding-top: 60px;}
   #arxiv {
     border-style: solid;
     border-width: 1px;
   }
</style>


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
<body>"""

  def nav(part1: String, part2: String, part3: String) =
    s"""
    |<nav class="navbar navbar-default navbar-fixed-top">
    |    <div class="container-fluid">
    |      <!-- Brand and toggle get grouped for better mobile display -->
    |      <div class="navbar-header">
    |        <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1" aria-expanded="false">
    |          <span class="sr-only">Toggle navigation</span>
    |          <span class="icon-bar"></span>
    |          <span class="icon-bar"></span>
    |          <span class="icon-bar"></span>
    |        </button>
    |        <span class="navbar-brand">Probability and Statistics (notes by Manjunath Krishnapur)</span>
    |      </div>
    |
    |      <!-- Collect the nav links, forms, and other content for toggling -->
    |      <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
    |
    |        <ul class="nav navbar-nav navbar-right">
    |
    |          <li class="dropdown">
    |            <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Part 1 <span class="caret"></span></a>
    |            <ul class="dropdown-menu">
    |             $part1
    |            </ul>
    |          </li>
    |
    |          <li class="dropdown">
    |            <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Part 2 <span class="caret"></span></a>
    |            <ul class="dropdown-menu">
    |             $part2
    |            </ul>
    |          </li>
    |
    |          <li class="dropdown">
    |            <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Part 3 <span class="caret"></span></a>
    |            <ul class="dropdown-menu">
    |             $part3
    |            </ul>
    |          </li>
    |        </ul>
    |      </div><!-- /.navbar-collapse -->
    |    </div><!-- /.container-fluid -->
    |  </nav>
    |
  """.stripMargin

  val banner =
    """
<div class="container">
<div class="bg-primary">
     <div class="banner">

     <center><h2 style="margin-top: 2px;"> Probability and Statistics </h2></center>
     <center><h4 style="margin-bottom: 2px;"> Notes by Manjunath Krishnapur </h4></center>
   </div>
 </div>
 <section>
<p>&nbsp;</p>

"""

  val foot =
    """
</div>
|<script src="//ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>
 |<script type="text/javascript" src='../js/bootstrap.min.js'></script>
 |
  </body>
</html>
"""

  val defReg: Regex =
    "(\\\\def|\\\\newcommand|\\\\renewcommand)(\\\\[a-zA-Z0-9]+)([^\n%]+)".r

  val newCommReg: Regex = "(\\\\newcommand\\{)(\\\\[a-zA-Z0-9]+)(\\}[^\n%]+)".r

  val renewCommReg: Regex =
    "(\\\\renewcommand\\{)(\\\\[a-zA-Z0-9]+)(\\}[^\n%]+)".r

  def trimLine(l: String): String =
    if (l.startsWith("%")) ""
    else
      """[^\\]%""".r
        .findFirstMatchIn(l)
        .map((m) => m.before.toString + m.group(0).head)
        .getOrElse(l)

  def readTrim(f: Path): String =
    read.lines(f).map(trimLine).mkString("", "\n", "\n")

  val wd: Path = pwd / 'sitebuilder / "resources"

  val preamble: String = readTrim(wd / "mypreamble_2017.tex")

  val texFile: String = readTrim(wd / "stat-and-prob.tex")

  val fullText: String = texFile.replace("\\input{mypreamble_2017}", preamble)

  val Array(header: String, textPadded: String) =
    fullText.split("\\\\begin\\{document\\}")

  val text: String = textPadded
    .split("""\\maketitle""")
    .last
    .split("""\\end\{document\}""")
    .head
    .replace("<", " < ")
    .replace(">", " > ")
    .replace("~", " ")
    .replace("\\newpage", "")
    .replace("\\vspace{4mm}", "")

  val begReg: Regex = """\\begin\{([^\}\{]+|[^\{\}]*\{[^\{\}]*\}[^\{\}]*)\}""".r

  val fullBegReg: Regex =
    """\\begin\{([a-zA-Z0-9\*]*)\}([\s]*\[[^\[\]]+\])?([\s]*\\label\{[a-zA-Z0-9:+-_]+\})?""".r

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

  def recRplBegins(
      txt: String,
      head: String = "",
      thmCounter: Int = 0,
      labels: Map[String, Int] = Map()): (String, Int, Map[String, Int]) = {
    fullBegReg
      .findFirstMatchIn(txt)
      .map { (m) =>
        val title = Option(m.group(2))
        val labelOpt =
          Option(m.group(3)).map(_.trim.drop("""\label{""".size).dropRight(1))
        val newCounter =
          if (thmEnvs.keySet.contains(m.group(1))) thmCounter + 1
          else thmCounter
        val newLabels =
          if (thmEnvs.keySet.contains(m.group(1)))
            labelOpt.map((l) => labels + (l -> newCounter)).getOrElse(labels)
          else labels
        val newString =
          if (thmEnvs.keySet.contains(m.group(1)))
            s"""<div id="theorem-${newCounter}"><div class="panel panel-info ${m
              .group(1)}"> <div class="panel-heading">${thmEnvs(m.group(1))} $newCounter ${title
              .map((s) => "(" + s.drop(1).dropRight(1) + ")")
              .getOrElse("")}</div><div class="panel-body">"""
          else if (mathEnvs.contains(m.group(1)))
            Regex.quoteReplacement(
              "$$" + m.group(0).replace("""\label""", """\tag"""))
          else if (divEnvs.contains(m.group(1)))
            s"""<div class="${m.group(1)}">"""
          else Regex.quoteReplacement(m.group(0))
        recRplBegins(m.after.toString,
                     head + m.before.toString + newString,
                     newCounter,
                     newLabels)
      }
      .getOrElse((head + txt, thmCounter, labels))
  }

  def rplBegins(txt: String): (String, Map[String, Int]) =
    (recRplBegins(txt)._1
       .replace("""\\begin""", """\begin""")
       .replace("""\$""", "$"),
     recRplBegins(txt)._3)

  def replaceEnds(txt: String) =
    endReg.replaceAllIn(
      txt,
      (m) =>
//        if (inMath(m.start, txt) || inDisplayMath(m.start, txt)) {println(s"${m.group(0)} in math environment"); m.group(0)}
//        else
        if (mathEnvs.contains(m.group(1)))
          Regex.quoteReplacement(m.group(0) + "$$")
        else if (thmEnvs.keySet.contains(m.group(1))) "</div></div></div>"
        else if (divEnvs.contains(m.group(1))) "</div>"
        else Regex.quoteReplacement(m.group(0))
    )

  def replaceBlanks(txt: String): String =
    blankLineReg.replaceAllIn(
      txt,
      (m) =>
        if (doldolReg.findAllIn(m.before).size % 2 == 1) {
          print(m.before.toString.drop(m.before.toString.size - 25));
          println(m.after.toString.take(25)); m.group(0)
        } else "</p>\n<p class=\"text-justify\">"
    )

  def replaceSec(txt: String) =
    """(\\section\{)([^\}\{]+|[^\{\}]*\{[^\{\}]*\}[^\{\}]*)\}""".r
      .replaceAllIn(
        txt,
        (m) => s"""<h2>${m.group(2)}</h2><p class="text-justify">""")

  def replaceSubSec(txt: String) =
    """(\\subsection\{)([^\}\{]+|[^\{\}]*\{[^\{\}]*\}[^\{\}]*)\}""".r
      .replaceAllIn(
        txt,
        (m) => s"""<strong>${m.group(2)} .</strong><p class="text-justify">""")

  def recReplaceSubSection(txt: String,
                           head: String,
                           counter: Int,
                           section: Int): String =
    """(\\subsection\{)([^\}\{]+|[^\{\}]*\{[^\{\}]*\}[^\{\}]*)\}""".r
      .findFirstMatchIn(txt)
      .map { (m) =>
        val newHead =
          s"""<strong>${section}.${counter + 1} ${m.group(2)}.</strong><p class="text-justify">"""
        recReplaceSubSection(m.after.toString, newHead, counter + 1, section)
      }
      .getOrElse(head + txt)

  def recReplaceSection(
      txt: String,
      head: String = "",
      counter: Int = 0,
      secs: Map[Int, String] = Map()): (String, Map[Int, String]) =
    """(\\section\{)([^\}\{]+|[^\{\}]*\{[^\{\}]*\}[^\{\}]*)\}""".r
      .findFirstMatchIn(txt)
      .map { (m) =>
        val title = m.group(2).toString
        val prev = recReplaceSubSection(m.before.toString, "", 0, counter)
        val newHead =
          s"""$head$prev</section>
             <section><h2 id="section-${counter + 1}">${counter + 1}. ${title}.</h2><p class="text-justify">""".stripMargin
        recReplaceSection(
          m.after.toString,
          newHead,
          counter + 1,
          secs + ((counter + 1) -> title)
        )
      }
      .getOrElse(head + txt -> secs)

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
      (m) =>
        if (dolReg
              .findAllIn(txt)
              .size % 2 == 0 && dolReg.findAllIn(txt).size % 2 == 0)
          Regex.quoteReplacement(s"<strong>${m.group(2)}</strong>")
        else m.group(2))
    bfReg2.replaceAllIn(
      step,
      (m) =>
        if (dolReg
              .findAllIn(txt)
              .size % 2 == 0 && dolReg.findAllIn(txt).size % 2 == 0)
          Regex.quoteReplacement(s"<strong>${m.group(2)}</strong>")
        else m.group(2))
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
    replaceEnds(
      replaceEm(
        replaceBf(
          replacePara(replaceParag(replaceBlanks(replaceItems(defReplaced)))))))

  lazy val (begReplaced, labels) = rplBegins(baseReplaced)

  lazy val refReplaced =
    """\\ref\{([a-zA-Z0-9:+-_]+)\}""".r.replaceAllIn(
      begReplaced,
      (m) =>
        labels
          .get(m.group(1))
          .map((n) => s"""<a href="#theorem-$n">$n</a>""")
          .getOrElse(m.group(0)))

  lazy val (allReplaced, sections) = recReplaceSection(refReplaced)

  lazy val sortedSections: Vector[(Int, String)] = sections.toVector.sortBy(_._1)

  def sectionList(v: Vector[(Int, String)]): String =
    v.map {
        case (n, title) =>
          s"""<li><a href="#section-${n}">${n}. $title</a> </li>"""
      }
      .mkString("\n")

  lazy val newFile = header + """\begin{document}""" + defReplaced

  def replace() = write.over(wd / "repl.tex", newFile)

  lazy val draftHtml = top + nav(
    sectionList(sortedSections.take(15)),
    sectionList(sortedSections.drop(15).take(15)),
    sectionList(sortedSections.drop(30))) + banner + allReplaced + foot

  def html() = write.over(pwd / "docs" / "draft" / "index.html", draftHtml)
}

object CrudeBuild extends App {
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
