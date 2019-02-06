import com.typesafe.sbt.SbtNativePackager.autoImport.NativePackagerHelper._

organization in ThisBuild := "biz.lobachev"
version in ThisBuild := "0.1.0-SNAPSHOT"
maintainer in ThisBuild := "valery@lobachev.biz"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.6"

lazy val root = (project in file("."))
  .settings(name := "annette-axon")
  .aggregate(
    `axon-backend`,
    `annette-shared`,
    `annette-security`,
    `bpm-repository-api`,
    `bpm-repository-impl`,
    `bpm-engine-api`,
    `bpm-engine-impl`,
    `knowledge-repository-api`,
    `knowledge-repository-impl`,
    `authorization-api`,
    `authorization-impl`
  )
  .settings(commonSettings: _*)

lazy val `axon-backend` = (project in file("axon-backend"))
  .settings(commonSettings: _*)
  .enablePlugins(PlayScala, LagomPlay, SbtReactiveAppPlugin)
  //.disablePlugins(PlayFilters)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslServer,
      ws,
      Dependencies.macwire,
      "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
    ) ++ Dependencies.tests
  )
  .dependsOn(
    `bpm-repository-api`,
    `bpm-engine-api`,
    `knowledge-repository-api`,
    `annette-security`
  )

lazy val `annette-shared` = (project in file("annette-shared"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      lagomScaladslServer % Optional,
      Dependencies.jwt
    ) ++ Dependencies.tests
  )

lazy val `annette-security` = (project in file("annette-security"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      lagomScaladslServer % Optional,
      Dependencies.jwt
    ) ++ Dependencies.tests
  )
  .dependsOn(`authorization-api`, `annette-shared`)

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
      Dependencies.macwire
    ) ++ Dependencies.tests,
    unmanagedClasspath in Runtime += baseDirectory.value / "conf",
    mappings in Universal ++= directory(baseDirectory.value / "conf"),
    scriptClasspath := "../conf/" +: scriptClasspath.value,
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
      Dependencies.macwire
    ) ++ Dependencies.tests,
    unmanagedClasspath in Runtime += baseDirectory.value / "conf",
    mappings in Universal ++= directory(baseDirectory.value / "conf"),
    scriptClasspath := "../conf/" +: scriptClasspath.value,
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`bpm-repository-api`)

lazy val `bpm-engine-api` = (project in file("bpm-engine-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
    ) ++ Dependencies.bpmEngine
  )
  .dependsOn(`annette-shared`, `bpm-repository-api`)

lazy val `bpm-engine-impl` = (project in file("bpm-engine-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      Dependencies.macwire
    ) ++ Dependencies.tests,
    unmanagedClasspath in Runtime += baseDirectory.value / "conf",
    mappings in Universal ++= directory(baseDirectory.value / "conf"),
    scriptClasspath := "../conf/" +: scriptClasspath.value,
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`bpm-engine-api`, `bpm-repository-api`)

lazy val `knowledge-repository-api` = (project in file("knowledge-repository-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
    ) ++ Dependencies.tests
  )
  .dependsOn(`annette-shared`)

lazy val `knowledge-repository-impl` = (project in file("knowledge-repository-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      Dependencies.macwire
    ) ++ Dependencies.tests,
    unmanagedClasspath in Runtime += baseDirectory.value / "conf",
    mappings in Universal ++= directory(baseDirectory.value / "conf"),
    scriptClasspath := "../conf/" +: scriptClasspath.value,
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`knowledge-repository-api`)

def commonSettings: Seq[Setting[_]] = Seq(
  )

lagomCassandraCleanOnStart in ThisBuild := false

// Kafka not used yet
lagomKafkaEnabled in ThisBuild := false
