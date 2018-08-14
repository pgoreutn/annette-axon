import sbt._

import play.sbt.PlayImport._
object Dependencies {
  object Version {
    val scala = "2.12.6"
    val akka = "2.5.14"
    val akkaPersistenceCassandra = "0.86"
    val akkaPersistenceInmemoryVersion = "2.5.1.1"

    val alpakka = "0.20"
  }

  val persistence: Seq[sbt.ModuleID] = Seq (
  "com.typesafe.akka" %% "akka-persistence" % Version.akka,
  "com.typesafe.akka" %% "akka-persistence-cassandra" % Version.akkaPersistenceCassandra,
  "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % Version.akkaPersistenceCassandra % Test
  )

  val tests = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
    "com.github.dnvriend" %% "akka-persistence-inmemory" % Version.akkaPersistenceInmemoryVersion % Test,
    "commons-io" % "commons-io" % "2.5" % Test
  )

  val core = persistence ++ tests :+ guice :+ ws
}
