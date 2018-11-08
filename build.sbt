organization in ThisBuild := "biz.lobachev"
version in ThisBuild := "0.1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.6"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test

lazy val root = (project in file("."))
  .settings(name := "annette-axon")
  .aggregate(`axon-backend`, `annette-shared`, `bpm-repository-api`, `bpm-repository-impl`, `authorization-api`, `authorization-impl`)
  .settings(commonSettings: _*)


lazy val `axon-backend` = (project in file("axon-backend"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayScala, LagomPlay, SbtReactiveAppPlugin)
  //.disablePlugins(PlayFilters)
  .dependsOn(`bpm-repository-api`)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslServer,
      macwire,
      ws,
      scalaTest,
      "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
    )
  )

lazy val `annette-shared` = (project in file("annette-shared"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      lagomScaladslServer % Optional,
      scalaTest,
      Dependencies.jwt
    )
  )


lazy val `authorization-api` = (project in file("authorization-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
  .dependsOn(`annette-shared`)

lazy val `authorization-impl` = (project in file("authorization-impl"))
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
  .dependsOn(`authorization-api`)




lazy val `bpm-repository-api` = (project in file("bpm-repository-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
  .dependsOn(`annette-shared`)

lazy val `bpm-repository-impl` = (project in file("bpm-repository-impl"))
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
  .dependsOn(`bpm-repository-api`)


def commonSettings: Seq[Setting[_]] = Seq(
)

lagomCassandraCleanOnStart in ThisBuild := false

// Kafka not used yet
lagomKafkaEnabled in ThisBuild := false
