import com.typesafe.sbt.SbtMultiJvm.multiJvmSettings
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm

name := "akka-in-action"

version := "1.0"

scalaVersion := "2.12.4"

lazy val akkaVersion = "2.5.6"

lazy val akkaHttpVersion = "10.0.10"

lazy val `akka-in-action` = project
  .in(file("."))
  .settings(multiJvmSettings: _*)
  .settings(
    organization := "com.damoshow",
    scalaVersion := "2.12.3",
    scalacOptions in Compile ++= Seq("-deprecation", "-feature", "-unchecked", "-Xlog-reflective-calls", "-Xlint"),
    javacOptions in Compile ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
    javaOptions in run ++= Seq("-Xms128m", "-Xmx1024m", "-Djava.library.path=./target/native"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-remote" % akkaVersion,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
      "io.swagger" % "swagger-jaxrs" % "1.5.16",
      "com.github.swagger-akka-http" %% "swagger-akka-http" % "0.11.0",
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
      "ch.megard" %% "akka-http-cors" % "0.2.1",
      "org.slf4j" % "slf4j-simple" % "1.7.25",
      "com.twitter" %% "chill-akka" % "0.9.2",
      "de.heikoseeberger" %% "akka-http-circe" % "1.18.0",
      "com.github.nscala-time" %% "nscala-time" % "2.14.0",
      "org.typelevel" %% "cats" % "0.9.0",
      "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion % Test,
      "org.scalatest" %% "scalatest" % "3.0.1" % "test"
    ),
    fork in run := true,
    mainClass in (Compile, run) := Some("com.damoshow.aia.firstcluster.Main"),
    // disable parallel tests
    parallelExecution in Test := false,
    licenses := Seq(("CC0", url("http://creativecommons.org/publicdomain/zero/1.0")))
  )
  .configs(MultiJvm)


