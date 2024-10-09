name := "Scala-Advanced-Exercises"

version in ThisBuild := "1.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.12.17"

scalacOptions in ThisBuild ++= Seq("-deprecation", "-feature")

libraryDependencies in ThisBuild ++= Seq(
  "org.scala-lang.modules" %% "scala-xml" % "1.1.0",
  "org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "com.google.guava" % "guava" % "23.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.jsuereth"  %% "scala-arm" % "2.0",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2",
  "org.typelevel" %% "cats-core" % "1.6.0",
  "org.typelevel" %% "cats-effect" % "1.3.1",
  "org.typelevel" %% "cats-free" % "1.6.0",
  "com.typesafe.play" %% "play-json" % "2.7.3",
  "org.scalactic" %% "scalactic" % "3.0.7",
  "org.scalatest" %% "scalatest" % "3.0.7" % Test,
  "com.typesafe.akka" %% "akka-actor" % "2.5.23",
  "com.typesafe.akka" %% "akka-remote" % "2.5.23",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.23" % Test
)

lazy val module12 = project
lazy val module13 = project
lazy val module14 = project
lazy val module15 = project
lazy val module16 = project
