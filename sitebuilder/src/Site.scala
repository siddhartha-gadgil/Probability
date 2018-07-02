package probability

import ammonite.ops
import ops._

import scala.xml.Elem

object Site {


  val mathjax =
    """
      |<!-- mathjax config similar to math.stackexchange -->
      |<script type="text/x-mathjax-config">
      |MathJax.Hub.Config({
      |jax: ["input/TeX", "output/HTML-CSS"],
      |tex2jax: {
      |  inlineMath: [ ['$', '$'] ],
      |  displayMath: [ ['$$', '$$']],
      |  processEscapes: true,
      |  skipTags: ['script', 'noscript', 'style', 'textarea', 'pre', 'code']
      |},
      |messageStyle: "none",
      |"HTML-CSS": { preferredFont: "TeX", availableFonts: ["STIX","TeX"] }
      |});
      |</script>
      |<script type="text/javascript" async
      |      src="https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.1/MathJax.js?config=TeX-MML-AM_CHTML">
      |       </script>
    """.stripMargin

  def head(relDocsPath: String): String =
    s"""
       |<head>
       |    <meta charset="utf-8">
       |    <meta http-equiv="X-UA-Compatible" content="IE=edge">
       |    <meta name="viewport" content="width=device-width, initial-scale=1">
       |    <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
       |    <title>MA 261: Probability Models</title>
       |    <link rel="icon" href="${relDocsPath}IIScLogo.jpg">
       |
       |    <!-- Bootstrap -->
       |    <link href="${relDocsPath}css/bootstrap.min.css" rel="stylesheet">
       |   <link href="${relDocsPath}css/katex.min.css" rel="stylesheet">
       |   <link href="${relDocsPath}css/main.css" rel="stylesheet">
       |
       |
       |    <link rel="stylesheet" href="${relDocsPath}css/zenburn.css">
       |    <script src="${relDocsPath}js/highlight.pack.js"></script>
       |    <script>hljs.initHighlightingOnLoad();</script>
       |
       |   <script src="${relDocsPath}js/ace.js"></script>
       |   <script src="${relDocsPath}js/katex.min.js"></script>
       |
       |    $mathjax
       |  </head>
       |
   """.stripMargin

  def nav(relDocsPath: String = ""): Elem =
    <nav class="navbar navbar-default">
      <div class="container-fluid">
        <!-- Brand and toggle get grouped for better mobile display -->
        <div class="navbar-header">
          <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1" aria-expanded="false">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </button>
          <span class="navbar-brand">Instructor: <a href="http://math.iisc.ac.in/~gadgil" target="_blank">Siddhartha Gadgil</a></span>
        </div>

        <!-- Collect the nav links, forms, and other content for toggling -->
        <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
          <ul class="nav navbar-nav" id="left-nav">
            <li><a href={s"${relDocsPath}index.html"}>Docs Home</a></li>
            <li class="dropdown">
              <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">
                Assignments<span class="caret"></span></a>
              <ul class="dropdown-menu">
                {assList(relDocsPath)}
              </ul>
            </li>
          </ul>
          <ul class="nav navbar-nav navbar-right">
            <li> <a href={s"${relDocsPath}notes/index.html"} target="_blank">Notes</a></li>
          </ul>
        </div><!-- /.navbar-collapse -->
      </div><!-- /.container-fluid -->
    </nav>

  def foot(relDocsPath: String): String =
    s"""
       |<div class="container-fluid">
       |  <p>&nbsp;</p>
       |  <p>&nbsp;</p>
       |  <p>&nbsp;</p>
       | <div class="footer navbar-fixed-bottom navbar-default">
       |  <h4> &nbsp;<a href="http://math.iisc.ac.in" target="_blank">&nbsp; Department of Mathematics,</a>
       | &nbsp;<a href="http://iisc.ac.in" target="_blank">Indian Institute of Science.</a></h4>
       | </div>
       |</div>
       |<script type="text/javascript" src="${relDocsPath}js/jquery-2.1.4.min.js"></script>
       |<script type="text/javascript" src="${relDocsPath}js/bootstrap.min.js"></script>
       |<script type="text/javascript" src="${relDocsPath}js/probability.js"></script>
       |<script>
       |  provingground.main()
       |</script>
   """.stripMargin


  def threeDash(s: String) = s.trim == "---"

  def withTop(l: Vector[String]) =
    (l.filter(threeDash).size == 2) && threeDash(l.head)

  def body(l: Vector[String]) =
    if (withTop(l)) l.tail.dropWhile((l) => !threeDash(l)).tail else l

  def topmatter(lines: Vector[String]) =
    if (withTop(lines)) Some(lines.tail.takeWhile((l) => !threeDash(l)))
    else None

  def titleOpt(l: Vector[String]): Option[String] =
    for {
      tm <- topmatter(l)
      ln <- tm.find(_.startsWith("title: "))
    } yield ln.drop(6).trim

  def filename(s: String) = s.toLowerCase.replaceAll("\\s", "-")


  def dateOpt(l: Vector[String]): Option[(Int, Int, Int)] =
    for {
      tm <- topmatter(l)
      m <- """date: (\d\d\d\d)-(\d\d)-(\d\d)""".r.findFirstMatchIn(
        tm.mkString("\n"))
    } yield (m.group(1).toInt, m.group(2).toInt, m.group(3).toInt)

  case class Assignment(name: String,
                  content: String,
                  optDate: Option[(Int, Int, Int)],
                  optTitle: Option[String]) {
    lazy val title = optTitle.getOrElse(name)

    lazy val dateString =
      optDate.map { case (y, m, d) => s"$y-$m-$d-" }.getOrElse("")

    val target = pwd / "docs" / "assignments" / s"$name.html"

    def url(relDocsPath: String) = s"${relDocsPath}assignments/$name.html"

    val date: (Int, Int, Int) = optDate.getOrElse((0, 0, 0))

    def output: String =
      page(content, "../")

    def save = write.over(target, output)
  }

  def getAss(p: Path): Assignment = {
    val l = ops.read.lines(p).toVector
    val name =
      titleOpt(l).map(filename).getOrElse(p.name.dropRight(p.ext.length + 1))
    val content = body(l).mkString("\n")
    Assignment(name, content, dateOpt(l), titleOpt(l))
  }

  def assDir = pwd / "sitebuilder" / "resources" / "assignments"

  def allAss: Seq[Assignment] =
    ls(assDir).map(getAss).sortBy(_.date).reverse

  def assList(relDocsPath: String): Seq[Elem] =
      allAss.map(
        (ass) =>
          <li><a href={s"${ass.url(relDocsPath)}"}>{ass.title}</a></li>
      )


  def page(s: String,
           relDocsPath: String): String =
    s"""
       |<!DOCTYPE html>
       |<html lang="en">
       |${head(relDocsPath)}
       |<body>
       |${nav(relDocsPath)}
       |<div class="container">
       |
       |$s
       |
       |</div>
       |${foot(relDocsPath)}
       |</body>
       |</html>
   """.stripMargin

val home =
  <section>
  <div class="col-md-9">

      <h1>UM 102: Analysis and Linear Algebra II </h1>
    <div id="desc" class="section">
      <h3> Course Syllabus </h3>
        <p>
          Linear Algebra continued: Inner products and Orthogonality; Determinants; Eigenvalues and
          Eigenvectors; Diagonalisation of Symmetric matrices. Multivariable calculus: Functions on $R^n$
        </p>
        <p>
          Partial and Total derivatives; Chain rule; Maxima, minima and saddles; Lagrange multipliers;
          Integration in $R^n$, change of variables, Fubini's theorem; Gradient, Divergence and Curl; Line
          and Surface integrals in $R^2$
          and $R^3$
          ; Stokes, Green's and Divergence theorems.
        </p>
        <p>
          Introduction to Ordinary Differential Equations; Linear ODEs and Canonical forms for linear
          transformations.
        </p>
    </div>
    <div class="section" id ="Assignments">
      <h3> <a href="assign-all.html">Assignments</a> </h3>
      <p>Assignments will be posted roughly once a week.</p>
      <ul>
        {allAss.map((ass) =>
        <li>
          <a href={ass.url("")}> {ass.title}</a>, due by {ass.dateString}.
        </li>)
      }
      </ul>
    </div>
    <div id="refs" class="section">
      <h3>Suggested books</h3>
      <ol>
        <li> T. M. Apostol, Calculus, Volume II, 2nd. Edition, Wiley Wiley India, 2007.</li>
        <li> Kalyan Mukherjea, Differential Calculas in Normed Linear Spaces (Texts and Readings in Mathematics), Hindustan Book Agency, 2007. </li>
        <li> M. Spivak, Calculus on Manifolds. </li>
        <li> G. Strang, Linear Algebra And Its Applications, 4th Edition, Brooks/Cole, 2006</li>
        <li> M. Artin, Algebra, Prentice Hall of India, 1994. </li>
        <li> M. Hirsch, S. Smale, R. L. Devaney, Differential Equations, Dynamical Systems, and an
        Introduction to Chaos, 2nd Edition, Academic Press, 2004. </li>
      </ol>
    </div>
    <div id ="details" class="section">
      <h3> Course Details</h3>
      <ul>
        <li><strong>Instructor:</strong> <a href="http://math.iisc.ac.in/~gadgil" target="_blank"> Siddhartha Gadgil</a></li>
        <li><strong>E-mail:</strong> <a href="mailto:siddhartha.gadgil@gmail.com" target="_blank"> siddhartha.gadgil@gmail.com</a>.</li>
        <li> <strong>Office:</strong> N-15, Department of Mathematics. </li>
        <li><strong>Timing: </strong>
          <ul>
            <li><strong>Lectures:</strong> Mon-Wed-Fri: 9:30 a.m. to 10:30 a.m.</li>
            <li> <strong>Tutorials:</strong> Mon: 12:00 noon - 1:00 p.m. </li>
          </ul>
        </li>
        <li><strong>Lecture Venue: </strong> Undergraduate main lecture hall</li>
        <li><strong> Teaching Assistants: </strong>
          <ul>
            <li>
              <a href="mailto:abusufian@iisc.ac.in" target="_blank"> Abu Sufian</a> (group <strong>A</strong>); <strong>office:</strong> N-07.
            </li>
            <li>
              <a href="mailto:anindyab@iisc.ac.in" target="_blank"> Anindya Biswas</a> (group <strong>B</strong>); <strong>office:</strong> R-22;
              <strong>office hours:</strong> Fri 9:00 pm to 10:00 pm.
            </li>
            <li>
              <a href="mailto:tejag@iisc.ac.in" target="_blank"> G. V. K. Teja</a> (group <strong>C</strong>); <strong>office:</strong> R-21.
            </li>
            <li>
              <a href="mailto:prateekv@iisc.ac.in" target="_blank"> Prateek Vishwakarma</a> (group <strong>D</strong>); <strong>office:</strong> R-24.
            </li>
            <li>
              <a href="mailto:amarsarkar@iisc.ac.in" target="_blank"> Amardeep Sarkar </a> (co-ordinator); <strong>office:</strong> L-12.
            </li>

          </ul>
        </li>
      </ul>
    </div>
  </div>

  <div class="col-md-3 section" id ="Alerts">
    <h4> <a href="assign-all.html">Upcoming Assignments</a> </h4>
    <ul>
      {allAss.map((ass) =>
      <li>
        <a href={ass.url("")}> {ass.title}</a>, due by {ass.dateString}.
      </li>)
    }
    </ul>

  </div>

  </section>

  def mkAss() = {
    allAss.foreach { (ass) =>
      pprint.log(s"saving assignment ${ass.name} due on ${ass.dateString}")
      write.over(ass.target, ass.output)
    }
  }


  def mkHome(): Unit =
    write.over(pwd / "docs" / "index.html", page(home.toString, ""))



}
