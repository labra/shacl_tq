import sbt._
import sbt.Keys._

lazy val root = project.in(file("."))

organization := "es.weso"

name := "shacl_tq"

scalaVersion := "2.11.8"

version := "0.0.1"

libraryDependencies ++= Seq(
    "commons-configuration" % "commons-configuration" % "1.7"
  , "org.rogach" %% "scallop" % "0.9.5" 
  , "org.scalatest" % "scalatest_2.11" % "3.0.0-M15" % "test"
  , "es.weso" % "srdf-jena_2.11" % "0.0.3" 
  , "es.weso" % "weso_utils_2.11" % "0.0.6" 
  , "es.weso" % "validating_2.11" % "0.0.6"   
  )

autoCompilerPlugins := true


// Binari packaging
enablePlugins(JavaAppPackaging)

packageSummary in Linux := "shacl_tq"
packageSummary in Windows := "shacl_tq"
packageDescription := "shacl_tq"

maintainer in Windows := "WESO"
maintainer in Debian := "Jose Emilio Labra <jelabra@gmail.es>"

mainClass in Compile := Some("es.weso.shacl_tq.Main")

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

//resolvers += "Sonatype OSS Snapshots" at
//  "https://oss.sonatype.org/content/repositories/snapshots"

// resolvers += "Bintray" at "http://dl.bintray.com/weso/weso-releases"

resolvers += Resolver.bintrayRepo("labra", "maven")

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Managed

// Publishing settings to BinTray

publishMavenStyle := true

bintrayRepository in bintray := "weso-releases"

bintrayOrganization in bintray := Some("weso")

licenses += ("MPL-2.0", url("http://opensource.org/licenses/MPL-2.0"))

resolvers += "Bintray" at "http://dl.bintray.com/weso/weso-releases"

resolvers += Resolver.bintrayRepo("labra", "maven")

ghpages.settings

git.remoteRepo := "git@github.com:labra/shacl_tq.git"

lazy val publishSettings = Seq(
  homepage := Some(url("https://github.com/labra/shacl_tq")),
  licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT")),
  scmInfo := Some(ScmInfo(url("https://github.com/labra/shacl_tq"), "scm:git:git@github.com:labra/shacl_tq.git")),
  autoAPIMappings := true,
  apiURL := Some(url("http://labra.github.io/shacl_tq/latest/api/")),
  pomExtra := (
    <developers>
      <developer>
        <id>labra</id>
        <name>Jose Emilio Labra Gayo</name>
        <url>https://github.com/labra/</url>
      </developer>
    </developers>
  ),
  scalacOptions in (Compile,doc) ++= Seq(
//    "-Xfatal-warnings",
    "-diagrams-debug",
    "-doc-source-url", scmInfo.value.get.browseUrl + "/tree/master€{FILE_PATH}.scala",
    "-sourcepath", baseDirectory.in(LocalRootProject).value.getAbsolutePath,
    "-diagrams"
  )
)
