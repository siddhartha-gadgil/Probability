import mill._, scalalib._, scalajslib._, define.Task
import ammonite.ops._

val scalaV = "2.13.3"
object probability extends Module{
  object jvm extends ScalaModule{
    def scalaVersion = scalaV
    def millSourcePath = super.millSourcePath / up
  }

  object js extends ScalaJSModule {
    def scalaVersion = scalaV
    def scalaJSVersion = "0.6.33"
    def millSourcePath = super.millSourcePath / up

    def platformSegment = "js"
  }
}

object sitebuilder extends ScalaModule{
  def scalaVersion = scalaV

  def moduleDeps = Seq(probability.jvm)

  def ivyDeps = Agg(
    ivy"com.lihaoyi:::ammonite:2.2.0"
  )

  def resources = T.sources {
    def base : Seq[Path] = super.resources().map(_.path)
    def jsout = client.fastOpt().path / up
    (base ++ Seq(jsout)).map(PathRef(_))
  }
}

object client extends ScalaJSModule{
  def scalaVersion = scalaV
  def scalaJSVersion = "0.6.33"
  def moduleDeps : Seq[ScalaJSModule] = Seq(probability.js)

  def platformSegment = "js"

  import coursier.maven.MavenRepository

  def repositories = super.repositories ++ Seq(
    MavenRepository("https://oss.sonatype.org/content/repositories/releases")
  )

  def ivyDeps = Agg(
    ivy"org.scala-js::scalajs-dom::1.1.0",
    ivy"in.nvilla::monadic-html::0.4.0",
    ivy"com.lihaoyi::scalatags::0.9.2"
  )

  def pack(): define.Command[PathRef] = T.command {
    def js = fastOpt()
    cp.over(js.path, pwd/ "docs" / "js" / "probability.js")
    js
  }


}
