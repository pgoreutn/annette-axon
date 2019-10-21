import com.typesafe.sbt.SbtNativePackager.autoImport.NativePackagerHelper._

// Copyright settings
def copyrightSettings: Seq[Setting[_]] = Seq(
  organizationName := "Valery Lobachev",
  startYear := Some(2018),
  licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),
)

organization in ThisBuild := "biz.lobachev"
version in ThisBuild := "0.1.0-SNAPSHOT"
maintainer in ThisBuild := "valery@lobachev.biz"

scalaVersion in ThisBuild := "2.12.9"

def commonSettings: Seq[Setting[_]] = Seq(
  unmanagedClasspath in Runtime += baseDirectory.value / "conf",
  mappings in Universal ++= directory(baseDirectory.value / "conf"),
  scriptClasspath := "../conf/" +: scriptClasspath.value,
)

lazy val root = (project in file("."))
  .settings(name := "annette-axon")
  .settings(copyrightSettings: _*)
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
    `authorization-impl`,
    `person-repository-api`,
    `person-repository-impl`,
    `org-structure-api`,
    `org-structure-impl`
  )
  

lazy val `axon-backend` = (project in file("axon-backend"))
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
  .settings(commonSettings: _*)
  .settings(copyrightSettings: _*)
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
      Dependencies.jwt,
      Dependencies.playJsonExt
      
    ) ++ Dependencies.tests ++ Dependencies.elastic 
  )
  .settings(copyrightSettings: _*)

lazy val `annette-security` = (project in file("annette-security"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      lagomScaladslServer % Optional,
      Dependencies.jwt
    ) ++ Dependencies.tests
  )
  .settings(copyrightSettings: _*)
  .dependsOn(`authorization-api`, `annette-shared`)

lazy val `authorization-api` = (project in file("authorization-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
  .settings(copyrightSettings: _*)
  .dependsOn(`annette-shared`)

lazy val `authorization-impl` = (project in file("authorization-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      Dependencies.macwire
    ) ++ Dependencies.tests,
  )
  .settings(lagomForkedTestSettings: _*)
  .settings(commonSettings: _*)
  .settings(copyrightSettings: _*)
  .dependsOn(`authorization-api`)

lazy val `bpm-repository-api` = (project in file("bpm-repository-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
  .settings(copyrightSettings: _*)
  .dependsOn(`annette-shared`, `knowledge-repository-api`)

lazy val `bpm-repository-impl` = (project in file("bpm-repository-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      Dependencies.macwire
    ) ++ Dependencies.tests,
  )
  .settings(lagomForkedTestSettings: _*)
  .settings(commonSettings: _*)
  .settings(copyrightSettings: _*)
  .dependsOn(`bpm-repository-api`)

lazy val `bpm-engine-api` = (project in file("bpm-engine-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
    ) ++ Dependencies.bpmEngine
  )
  .settings(copyrightSettings: _*)
  .dependsOn(`annette-shared`, `bpm-repository-api`, `knowledge-repository-api`)

lazy val `bpm-engine-impl` = (project in file("bpm-engine-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      Dependencies.macwire
    ) ++ Dependencies.tests,
  )
  .settings(lagomForkedTestSettings: _*)
  .settings(commonSettings: _*)
  .settings(copyrightSettings: _*)
  .dependsOn(`bpm-engine-api`, `bpm-repository-api`)

lazy val `knowledge-repository-api` = (project in file("knowledge-repository-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
    ) ++ Dependencies.tests
  )
  .settings(copyrightSettings: _*)
  .dependsOn(`annette-shared`)

lazy val `knowledge-repository-impl` = (project in file("knowledge-repository-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      Dependencies.macwire
    ) ++ Dependencies.tests,
    
  )
  .settings(lagomForkedTestSettings: _*)
  .settings(commonSettings: _*)
  .settings(copyrightSettings: _*)
  .dependsOn(`knowledge-repository-api`)

lazy val `person-repository-api` = (project in file("person-repository-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
  .settings(copyrightSettings: _*)
  .dependsOn(`annette-shared`, `annette-security`)

lazy val `person-repository-impl` = (project in file("person-repository-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      Dependencies.macwire
    ) ++ Dependencies.tests /*++ Dependencies.elastic*/ ,
  )
  .settings(lagomForkedTestSettings: _*)
  .settings(commonSettings: _*)
  .settings(copyrightSettings: _*)
  .dependsOn(`person-repository-api`)


  lazy val `org-structure-api` = (project in file("org-structure-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
  .settings(copyrightSettings: _*)
  .dependsOn(`annette-shared`, `annette-security`)

lazy val `org-structure-impl` = (project in file("org-structure-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslTestKit,
      Dependencies.macwire
    ) ++ Dependencies.tests /*++ Dependencies.elastic*/ ,
  )
  .settings(lagomForkedTestSettings: _*)
  .settings(commonSettings: _*)
  .settings(copyrightSettings: _*)
  .dependsOn(`org-structure-api`)

lagomCassandraCleanOnStart in ThisBuild := false

// Kafka not used yet
lagomKafkaEnabled in ThisBuild := false
