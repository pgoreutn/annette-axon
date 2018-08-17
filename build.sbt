organization in ThisBuild := "biz.lobachev"
version in ThisBuild := "0.1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.6"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test

lazy val root = (project in file("."))
  .settings(name := "annette-axon")
  .aggregate(`axon-backend`, `bpm-api`, `bpm-impl`)
  .settings(commonSettings: _*)


lazy val `axon-backend` = (project in file("axon-backend"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayScala, LagomPlay, SbtReactiveAppPlugin)
  .dependsOn(`bpm-api`)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslServer,
      macwire,
      ws,
      scalaTest,
      "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
    )
  )

lazy val `bpm-api` = (project in file("bpm-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `bpm-impl` = (project in file("bpm-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`bpm-api`)


def commonSettings: Seq[Setting[_]] = Seq(
)

lagomCassandraCleanOnStart in ThisBuild := false

// Kafka not used yet
lagomKafkaEnabled in ThisBuild := false
