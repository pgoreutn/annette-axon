name := """annette-axon"""
organization := "biz.lobachev"

version := "0.1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := Dependencies.Version.scala

libraryDependencies ++= guice +: Dependencies.core

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "biz.lobachev.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "biz.lobachev.binders._"

