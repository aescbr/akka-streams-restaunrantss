ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

lazy val root = (project in file("."))
  .settings(
    name := "akka-streams-restaurants"
  )
lazy val akkaVersion = "2.8.2"
lazy val scalaTestVersion = "3.0.5"

libraryDependencies ++= Seq(

  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.apache.kafka" % "kafka-clients" % "3.0.1",
  "io.circe" %% "circe-core" % "0.14.1",
  "io.circe" %% "circe-generic" % "0.14.1",
  "io.circe" %% "circe-parser" % "0.14.1" ,
  "com.goyeau" %% "kafka-streams-circe" % "fbee94b",
  "com.typesafe.akka" %% "akka-stream-kafka" % "3.1.0-M1",
  "com.lightbend.akka" %% "akka-stream-alpakka-csv" % "6.0.1",
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe" % "config" % "1.4.2",

  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.1.0" % Test,
  "org.scalatestplus" %% "mockito-4-6" % "3.2.15.0" % "test"
)
