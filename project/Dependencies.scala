import sbt._
import play.sbt.PlayImport._

object Dependencies {
  object Version {

    val scala = "2.12.6"

    val macwire = "2.3.1"

    val akka = "2.5.19"
    val akkaPersistenceCassandra = "0.02"

    val scalaTest = "3.0.5"
    val scalaTestPlusPlay = "3.1.2"
    val akkaPersistenceInmemoryVersion = "2.5.1.1"
    val commonsIO = "2.6"


    val jwtPlayJson = "1.1.0"

    val camunda = "7.10.0"
    val camundaSpin = "1.6.4"
    val groovy = "2.5.5"
    val pgDriver = "42.2.5"
  }


  val macwire = "com.softwaremill.macwire" %% "macros" % Version.macwire % "provided"

  val persistence: Seq[sbt.ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-persistence" % Version.akka,
    "com.typesafe.akka" %% "akka-persistence-cassandra" % Version.akkaPersistenceCassandra,
    "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % Version.akkaPersistenceCassandra % Test
  )

  val tests = Seq(
    "org.scalatest" %% "scalatest" % Version.scalaTest % Test,
    "org.scalatestplus.play" %% "scalatestplus-play" % Version.scalaTestPlusPlay % Test,
    "com.github.dnvriend" %% "akka-persistence-inmemory" % Version.akkaPersistenceInmemoryVersion % Test,
    "commons-io" % "commons-io" % Version.commonsIO % Test
  )

  val jwt: sbt.ModuleID = "com.pauldijou" %% "jwt-play-json" % Version.jwtPlayJson

  lazy val bpmEngine = Seq(
    "org.camunda.bpm" % "camunda-bom" % Version.camunda,
    "org.camunda.bpm" % "camunda-engine" % Version.camunda,
    "org.camunda.bpm" % "camunda-engine-plugin-spin" % Version.camunda,
    "org.camunda.spin" % "camunda-spin-core" % Version.camundaSpin,
    "org.camunda.spin" % "camunda-spin-dataformat-all" % Version.camundaSpin,
    "org.postgresql" % "postgresql" % Version.pgDriver,
    "org.codehaus.groovy" % "groovy-all" % Version.groovy // Groovy script engine
  )

  val core = persistence ++ tests :+ guice :+ ws :+ jwt
}
