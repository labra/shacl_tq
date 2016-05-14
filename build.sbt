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
  , "org.scalatest" % "scalatest_2.11" % "2.2.0" % "test"
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

