name := "SourceLoader"

version := "0.1.0"

organization := "com.api-tech"

scalaVersion := "2.10.0"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

licenses := Seq("Modified BSD" -> url("http://opensource.org/licenses/BSD-3-Clause"))

homepage := Some(url("https://bitbucket.org/apitech/sourceloader"))

seq(jacoco.settings : _*)

libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.10.0"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0.M5b" % "test"

libraryDependencies +=
    "org.scalamock" %% "scalamock-scalatest-support" % "3.0.1" % "test"

publishTo <<= version { v: String =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <scm>
    <url>https://bitbucket.org/apitech/sourceloader</url>
    <connection>scm:hg:https://apitech@bitbucket.org/apitech/sourceloader</connection>
  </scm>
  <developers>
    <developer>
      <id>smeeuwsen</id>
      <name>Scott Meeuwsen</name>
      <url>http://www.api-tech.com</url>
    </developer>
  </developers>
)