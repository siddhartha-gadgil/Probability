package probability

import ammonite.ops._

import scala.util.matching._

object TeXToHtml {
  val top =
    """
      |<html>
      |<head>
      |<meta charset="utf-8">
      |   <meta http-equiv="X-UA-Compatible" content="IE=edge">
      |   <meta name="viewport" content="width=device-width, initial-scale=1">
      |
      |<title> Probability Models and Stastics</title>
      |<link rel="icon" href="../IIScLogo.jpg">
      |
      |<!-- Latest compiled and minified CSS  for Bootstrap -->
      |<link rel="stylesheet" href="../css/bootstrap.min.css">
      |
      |<style type="text/css">
      |   body { padding-top: 60px; }
      |   .section {padding-top: 60px;}
      |   #arxiv {
      |     border-style: solid;
      |     border-width: 1px;
      |   }
      |</style>
      |
      |
      |  <!-- mathjax config similar to math.stackexchange -->
      |
      |
      |
      |  <script type="text/x-mathjax-config">
      |  MathJax.Hub.Config({
      |  TeX: { equationNumbers: { autoNumber: "AMS" } },
      |  jax: ["input/TeX", "output/HTML-CSS"],
      |  tex2jax: {
      |  inlineMath: [ ['$', '$'] ],
      |  displayMath: [ ['$$', '$$'], ['\\[', '\\]' ]],
      |  processEscapes: true,
      |  skipTags: ['script', 'noscript', 'style', 'textarea', 'pre', 'code']
      |  },
      |  messageStyle: "none",
      |  "HTML-CSS": { preferredFont: "TeX", availableFonts: ["STIX","TeX"] }
      |  });
      |  </script>
      |  <script type="text/javascript" async
      |      src="https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.1/MathJax.js?config=TeX-MML-AM_CHTML">
      |       </script>
      |</head>
      |<body>
    """.stripMargin

  def nav(part1: String, part2: String, part3: String): String =
    s"""
    |<nav class="navbar navbar-default navbar-fixed-bottom">
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
    |          <li><a href="index.html">Table of Contents</a></li>
    |          <li class="dropdown">
    |            <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false"> Probabibility (part 1) <span class="caret"></span></a>
    |            <ul class="dropdown-menu">
    |             $part1
    |            </ul>
    |          </li>
    |
    |          <li class="dropdown">
    |            <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Probability (part 2) <span class="caret"></span></a>
    |            <ul class="dropdown-menu">
    |             $part2
    |            </ul>
    |          </li>
    |
    |          <li class="dropdown">
    |            <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false"> Statistics <span class="caret"></span></a>
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

  val banner: String =
    """
      |<div class="container-fluid">
      |<div class="bg-primary">
      |     <div class="banner">
      |
      |     <center><h2 style="padding-top: 15px;"> Probability and Statistics </h2></center>
      |     <center><h4 style="padding-bottom: 15px;"> Notes by Manjunath Krishnapur </h4></center>
      |   </div>
      | </div>
      | </div>
      | <div class="container">
      | <section>
      |<p>&nbsp;</p>
    """.stripMargin

  val foot: String =
    """
      |</div>
      |<script type="text/javascript" src="../js/jquery-2.1.4.min.js"></script>
      | <script type="text/javascript" src='../js/bootstrap.min.js'></script>
      |<script type="text/javascript" src='../js/probability.js'></script>
      |<script type="text/javascript">
      |      Illustrations.main();
      |    </script>
      |
      |  </body>
      |</html>
    """.stripMargin

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
    .replace("\\vspace{2mm}", "")
    .replace("""\"{o}""", "&ouml;")

  val begReg: Regex = """\\begin\{([^\}\{]+|[^\{\}]*\{[^\{\}]*\}[^\{\}]*)\}""".r

  val fullBegReg: Regex =
    """\\begin\{([a-zA-Z0-9\*]*)\}([\s]*\[[^\[\]]+\])?([\s]*\\label\{[a-zA-Z0-9:+-_' ]+\})?""".r

  val endReg: Regex = """\\end\{([^\}\{]+|[^\{\}]*\{[^\{\}]*\}[^\{\}]*)\}""".r

  val converter = new TeXToHtml(header, text)

  val dolReg: Regex = "\\$".r

  val doldolReg: Regex = "\\$\\$".r

  val begItReg: Regex = """\\begin\{itemize\}""".r

  val endItReg: Regex = """\\end\{itemize\}""".r

  val begEnReg: Regex = """\\begin\{enumerate\}""".r

  val endEnReg: Regex = """\\begin\{enumerate\}""".r

  val blankLineReg: Regex = "\n[ \t]*\n[\\s]*".r

  def maxOpt[B: Ordering](v: Vector[B]): Option[B] =
    if (v.isEmpty) None else Some(v.max)

  val thmEnvs: Map[String, (String, String)] = Map(
    "example" -> ("Example", "info"),
    "question" -> ("Question", "danger"),
    "definition" -> ("Definition", "primary"),
    "remark" -> ("Remark", "success"),
    "exercise" -> ("Exercise", "warning"),
    "lemma" -> ("Lemma", "primary"),
    "problem" -> ("Problem", "danger"),
    "proposition" -> ("Proposition", "primary"),
    "theorem" -> ("Theorem", "primary")
  )

  val mathEnvs = Set(
    "align*",
    "align",
    "equation*",
    "equation"
  )

  val divEnvs = Set("proof")

  def indices(r: Regex, txt: String): Vector[Int] =
    r.findAllMatchIn(txt).toVector.map(_.start)

//  def dollarIndices(txt: String): Vector[Int] = indices(dolReg, txt).map(_ + 1)

//  def displayIndices(txt: String): Vector[Int] = indices(doldolReg, txt)

  def itemizeBegs(txt: String): Vector[Int] = indices(begItReg, txt)

  def itemizeEnds(txt: String): Vector[Int] = indices(endItReg, txt)

  def enumerateBegs(txt: String): Vector[Int] = indices(begEnReg, txt)

  def enumerateEnds(txt: String): Vector[Int] = indices(begEnReg, txt)

  def items(txt: String): Vector[Int] = indices("""\\item""".r, txt)

  def firstItem(j: Int, txt: String): Boolean = {
    val lastBeg = (enumerateBegs(txt) ++ itemizeBegs(txt)).filter(_ < j).max
    maxOpt(items(txt).filter(_ < j)).forall(_ < lastBeg)

  }

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
          Option(m.group(3)).map(_.trim.drop("""\label{""".length).dropRight(1))
        val newCounter =
          if (thmEnvs.keySet.contains(m.group(1))) thmCounter + 1
          else thmCounter
        val newLabels =
          if (thmEnvs.keySet.contains(m.group(1)))
            labelOpt.map((l) => labels + (l -> newCounter)).getOrElse(labels)
          else labels
        val newString =
          if (thmEnvs.keySet.contains(m.group(1)))
            s"""<div id="theorem-$newCounter"><div class="panel panel-${thmEnvs(
              m.group(1))._2} ${m
              .group(1)}"> <div class="panel-heading">${thmEnvs(m.group(1))._1} $newCounter ${title
              .map((s) => "(" + s.drop(1).dropRight(1) + ")")
              .getOrElse("")}</div><div class="panel-body">"""
          else if (mathEnvs.contains(m.group(1)))
//            Regex.quoteReplacement(
            "$$" + m.group(0) //.replace("""\label""", """tag""")
//            )
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

  def replaceEnds(txt: String): String =
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
          print(m.before.toString.drop(m.before.toString.length - 25))
          println(m.after.toString.take(25)); m.group(0)
        } else "</p>\n<p class=\"text-justify\">"
    )

  def replaceSec(txt: String): String =
    """(\\section\{)([^\}\{]+|[^\{\}]*\{[^\{\}]*\}[^\{\}]*)\}""".r
      .replaceAllIn(
        txt,
        (m) => s"""<h2>${m.group(2)}</h2><p class="text-justify">""")

  def replaceSubSec(txt: String): String =
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
          m.before + s"""<strong>$section.${counter + 1} ${m.group(2)}.</strong><p class="text-justify">"""
        recReplaceSubSection(m.after.toString, newHead, counter + 1, section)
      }
      .getOrElse(head + txt)

  def recReplaceSection(
      txt: String,
      head: String = "",
      counter: Int = 0,
      secs: Map[Int, String] = Map(),
      chapters: Map[Int, String] = Map()): (String, Map[Int, String], Map[Int, String]) =
    """(\\section\{)([^\}\{]+|[^\{\}]*\{[^\{\}]*\}[^\{\}]*)\}""".r
      .findFirstMatchIn(txt)
      .map { (m) =>
        val title = m.group(2).toString
        val prev = recReplaceSubSection(m.before.toString, "", 0, counter)
        val newHead =
          s"""$head$prev</section>
             <section><h2 id="section-${counter + 1}">${counter + 1}. $title.</h2><p class="text-justify">""".stripMargin
        recReplaceSection(
          m.after.toString,
          newHead,
          counter + 1,
          secs + ((counter + 1) -> title),
          chapters + (counter -> prev)
        )
      }
      .getOrElse((head + txt, secs, chapters + (counter -> txt)))

  def replacePara(txt: String): String =
    """(\\para\{)([^\}\{]+|[^\{\}]*\{[^\{\}]*\}[^\{\}]*)\}""".r
      .replaceAllIn(
        txt,
        (m) => s"<strong>${Regex.quoteReplacement(m.group(2))}:</strong>")

  def replaceParag(txt: String): String =
    """(\\parag\{)([^\}\{]+|[^\{\}]*\{[^\{\}]*\}[^\{\}]*)\}""".r
      .replaceAllIn(
        txt,
        (m) => s"<strong>${Regex.quoteReplacement(m.group(2))}</strong>")

  val bfReg1: Regex = "(\\{\\\\bf )([^\\}]+)\\}".r

  val bfReg2: Regex = "(\\{\\\\bf\\{)([^\\}]+)\\}\\}".r

  def replaceBf(txt: String): String = {
    val step = bfReg1.replaceAllIn(
      txt,
      (m) =>
        if (dolReg
              .findAllIn(m.before + " ")
              .size % 2 == 0 && doldolReg.findAllIn(m.before).size % 2 == 0)
          Regex.quoteReplacement(s"<strong>${m.group(2)}</strong>")
        else s"\\{\\\\bf ${m.group(2)}\\}"
    )
    bfReg2.replaceAllIn(
      step,
      (m) =>
        if (dolReg
              .findAllIn(m.before + " ")
              .size % 2 == 0 && doldolReg.findAllIn(m.before).size % 2 == 0)
          Regex.quoteReplacement(s"<strong>${m.group(2)}</strong>")
        else s"\\{\\\\bf ${m.group(2)}\\}"
    )
  }

  val emReg: Regex = "(\\{\\\\em)([ \\\\])([^\\}]+)\\}".r

  def replaceEm(txt: String): String =
    emReg.replaceAllIn(
      txt,
      (m) => Regex.quoteReplacement(s"<em>${m.group(2)}${m.group(3)}</em>"))

//  val undReg = "(\\{\\\\underline)([ \\\\])([^\\}]+)\\}".r

  def replaceUnderline(txt: String): String =
    """(\\underline\{)([^\}\{]+|[^\{\}]*\{[^\{\}]*\}[^\{\}]*)\}""".r
      .replaceAllIn(
        txt,
        (m) =>
          if (dolReg
                .findAllIn(m.before + " ")
                .size % 2 == 0 && doldolReg.findAllIn(m.before).size % 2 == 0)
            Regex.quoteReplacement(s"<u>${m.group(2)}</u>")
          else
            s"\\\\underline\\{${m.group(2).replace("\\", "\\\\")}\\}"
      )

  val tinyReg: Regex = "(\\{\\\\tiny )([^\\}]+)\\}".r

  def replaceTiny(txt: String): String =
    tinyReg.replaceAllIn(txt,
                         (m) => Regex.quoteReplacement(s"""{${m.group(2)}}"""))

  def bracesMatched(txt: String): Boolean = {
//    println(txt.length)
    val opens = """\{""".r.findAllIn(txt).length
    val closes = """\}""".r.findAllIn(txt).length
//    println(opens)
//    println(closes)
//    println(txt)
//    if (opens != closes) scala.io.StdIn.readLine()
    opens == closes
  }

  def footnote(txt: String, head: String): (String, String) = {
    """([^\\])(\})""".r.findFirstMatchIn(txt).map { (m) =>
      if (bracesMatched(head + m.before))
        (head+ m.before, m.after.toString)
      else {
        footnote(m.after.toString, head + m.before.toString + m.group(0))}
    }.getOrElse(throw new Exception("Unclosed brace for footnote"))
  }

  def recReplaceFootnotes(txt: String,
                          head: String = "",
                          counter: Int = 0
                          ): String =
    """\\footnote\{""".r
      .findFirstMatchIn(txt)
      .map { (m) =>
        val (note, rest) = footnote(m.after.toString, "")
        println("Footnote found")
        println(m.after.toString.take(50))
        val newHead =
          head + m.before + s"""<sup> <a data-toggle="collapse" href="#footnote-${counter + 1}" aria-expanded="false" aria-controls="footnote-${counter + 1}">
                                ${counter + 1}
                                 </a> </sup><span class="collapse small" id="footnote-${counter + 1}">$note</span> """.stripMargin
        recReplaceFootnotes(rest, newHead, counter + 1)
      }
      .getOrElse(head + txt)
}

class TeXToHtml(header: String, text: String) {
  import TeXToHtml._

  val defs: Vector[Regex.Match] = defReg.findAllMatchIn(header).toVector

  val newCommands: Vector[Regex.Match] = newCommReg.findAllMatchIn(header).toVector

  val renewCommands: Vector[Regex.Match] = renewCommReg
    .findAllMatchIn(header)
    .toVector ++ renewCommReg
    .findAllMatchIn(header)
    .toVector

  val defSubs: Vector[(String, String)] =
    (defs.map((m) => m.group(2) -> m.group(3).trim.drop(1).dropRight(1)) ++
      (newCommands ++ renewCommands).map((m) =>
        m.group(2) -> m.group(3).trim.drop(2).dropRight(1))).filterNot((c) =>
      Set("\\I", "\\matrices", "\\para", "\\parag").contains(c._1))

  def defReplace(txt: String): String = defSubs.foldLeft[String](txt) {
    case (t, (x, y)) =>
      new Regex(x.replace("\\", "\\\\") + "([^a-zA-Z0-9])")
        .replaceAllIn(t, (m) => Regex.quoteReplacement(y + m.group(1)))
  }

  def recDefReplace(txt: String): String = {
    val next = defReplace(txt)
    if (next == txt) next else recDefReplace(next)
  }

  lazy val defReplaced: String = recDefReplace(text).replace("""\noindent""", "")

  lazy val baseReplaced: String =
    replaceEnds(
      replaceTiny(
        replaceEm(replacePara(replaceParag(replaceItems(defReplaced))))))


  lazy val (begReplaced: String, labels: Map[String, Int]) = rplBegins(baseReplaced)

  lazy val refReplaced: String =
    """\\ref\{([a-zA-Z0-9:+-_]+)\}""".r.replaceAllIn(
      begReplaced,
      (m) =>
        labels
          .get(m.group(1))
          .map((n) => s"""<a href="#theorem-$n">$n</a>""")
          .getOrElse(m.group(0)))

  lazy val (secReplaced: String,
    sections: Map[Int, String],
    chapters: Map[Int, String]) = recReplaceSection(refReplaced)

  lazy val theoremChapters =
    {
      val regex = """(id="theorem-)([0-9]+)""".r
      chapters.toVector.map{case (n, txt) =>
        regex.findAllMatchIn(txt).map((m) =>
          (m.group(2).toInt -> n)
        ).toVector
      }.flatten
    }.toMap

  def refChapters(txt: String) = {
    """(href="#theorem-)([0-9]+)""".r
    .replaceAllIn(txt,
      (m) =>
        {
          val j = m.group(2).toInt
          s"""href="chapter-${theoremChapters(j)}.html#theorem-j"""
        })
  }

  lazy val allReplaced: String =
    recReplaceFootnotes(replaceUnderline(replaceBf(replaceBlanks(secReplaced))))

  lazy val chapReplaced =
    chapters.filter(_._1 > 0).mapValues{(chapter) =>
      recReplaceFootnotes(replaceUnderline(replaceBf(replaceBlanks(chapter))))
    }

  lazy val sortedSections: Vector[(Int, String)] =
    sections.toVector.sortBy(_._1)

  def sectionList(v: Vector[(Int, String)]): String =
    v.map {
        case (n, title) =>
          s"""<li><a href="#section-$n">$n. $title</a> </li>"""
      }
      .mkString("\n")

  def chapterList(v: Vector[(Int, String)]): String =
    v.map {
        case (n, title) =>
          s"""<li><a href="chapter-$n.html">$n. $title</a> </li>"""
      }
      .mkString("\n")

  def tocList : String =
    sortedSections.map {
        case (n, title) =>
          s"""<li><a href="chapter-$n.html">$title</a> </li>"""
      }
      .mkString("\n")

  lazy val newFile: String = header + """\begin{document}""" + defReplaced

  def replace(): Unit = write.over(wd / "repl.tex", newFile)

  lazy val chapNav = nav(
    chapterList(sortedSections.take(12)),
    chapterList(sortedSections.slice(12, 27)),
    chapterList(sortedSections.drop(27)))

  lazy val draftHtml: String = top + nav(
    sectionList(sortedSections.take(12)),
    sectionList(sortedSections.slice(12, 27)),
    sectionList(sortedSections.drop(27))) + banner + allReplaced + foot

  def chapLink(n: Int): String =
    sections.get(n).map{(t) =>
      s"""<div class="pull-right"><a href="chapter-$n.html" class="btn btn-primary">Chapter $n. $t</a></div>"""
    }.getOrElse("")

  lazy val chapHtml = chapReplaced.map{case (n, txt) =>
    (n,
      s"""$top
$chapNav
<div class="container">
<h1 class="text-center bg-info">Chapter $n : ${sections(n)}</h1>
<p>&nbsp;</p>
$txt
</p>
${chapLink(n + 1)}
<p>&nbsp;</p>
<p>&nbsp;</p>
<p>&nbsp;</p>
<p>&nbsp;</p>
<p>&nbsp;</p>
$foot"""
    )
  }

  lazy val tocHtml = s"""$top
$chapNav
$banner
<h1 class="text-center bg-info">Table of Contents</h1>
<p>&nbsp;</p>
<ol>
$tocList
</ol>
$foot"""


  def html(): Unit = {
    write.over(pwd / "docs" / "draft" / "index.html", draftHtml)
    chapHtml.foreach{
      case (n, html) =>
        write.over(pwd / "docs" / "chapters" / s"chapter-$n.html", html)
    }
    write.over(pwd / "docs" / "chapters" / "index.html", tocHtml)
  }
}

object TeXBuild extends App {
  import TeXToHtml._
  converter.html()
  val js = read(resource/"out.js")
  write.over(pwd / "docs" / "js" / "probability.js", js)
  println(converter.theoremChapters)
}
