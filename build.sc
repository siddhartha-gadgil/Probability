import mill._, scalalib._, scalajslib._, define.Task
import ammonite.ops._

trait MetalsModule extends ScalaModule{
  import java.io._

  def metalsBuildInfo = T{
    def targDeps : Agg[eval.PathRef] = resolveDeps(transitiveIvyDeps, false)()

    Map[String, String](
      "sources" -> allSourceFiles().map(_.path).mkString(java.io.File.pathSeparator),
      "unmanagedSourceDirectories" -> "",
      "managedSourceDirectories" -> "",
      "scalacOptions" -> scalacOptions().mkString(" "),
      "classDirectory" -> compile().classes.path.toString,
      "dependencyClasspath" ->
        (targDeps  ++
          Task.traverse(moduleDeps)(_.sources)().flatten
        ).map(_.path).mkString(java.io.File.pathSeparator),
      "scalaVersion" -> scalaVersion(),
      "sourceJars" ->
        resolveDeps(transitiveIvyDeps, true)().map(_.path).mkString(java.io.File.pathSeparator)
      )
  }

  def metalsConfig() = T.command{
      def outFile = pwd / ".metals" / "buildinfo" / RelPath(artifactName().toString) / "main.properties"
      def info = metalsBuildInfo()
      def output = info.map{
        case (k, v) => s"$k=$v"
      }.mkString("\n")
      write.over(outFile, output)
      output
    }
}

object probability extends Module{
  object jvm extends MetalsModule{
    def scalaVersion = "2.12.4"
    def millSourcePath = super.millSourcePath / up
  }

  object js extends ScalaJSModule with MetalsModule {
    def scalaVersion = "2.12.4"
    def scalaJSVersion = "0.6.22"
    def millSourcePath = super.millSourcePath / up

    def platformSegment = "js"
  }
}

object sitebuilder extends MetalsModule{
  def scalaVersion = "2.12.4"

  def moduleDeps = Seq(probability.jvm)

  def ivyDeps = Agg(
    ivy"com.lihaoyi:::ammonite:1.1.0"
  )

  def resources = T.sources {
    def base : Seq[Path] = super.resources().map(_.path)
    def jsout = client.fastOpt().path / up
    (base ++ Seq(jsout)).map(PathRef(_))
  }
}

object client extends ScalaJSModule with MetalsModule{
  def scalaVersion = "2.12.4"
  def scalaJSVersion = "0.6.22"
  def moduleDeps : Seq[ScalaJSModule] = Seq(probability.js)

  def platformSegment = "js"

  import coursier.maven.MavenRepository

  def repositories = super.repositories ++ Seq(
    MavenRepository("https://oss.sonatype.org/content/repositories/releases")
  )

  def ivyDeps = Agg(
    ivy"org.scala-js::scalajs-dom::0.9.4",
    ivy"in.nvilla::monadic-html::0.4.0-RC1"
  )

  def pack(): define.Command[PathRef] = T.command {
    def js = fastOpt()
    cp.over(js.path, pwd/ "docs" / "js" / "probability.js")
    js
  }


}
