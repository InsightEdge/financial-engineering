val insightEdgeVersion = "1.0.0"
val xapVersion = "12.0.1"

resolvers += Resolver.mavenLocal
resolvers += "Openspaces Maven Repository" at "http://maven-repository.openspaces.org"

logLevel := sbt.Level.Debug

val orgPrefix = "org.insightedge"
val coreName = "financial-engineering"

lazy val commonSettings = Seq(
  organization := s"$orgPrefix.$coreName",
  version := "1.0.0",
  scalaVersion := "2.10.6",
  test in assembly := {}
)

lazy val core = project.
  settings(commonSettings: _*).
  settings(assemblyJarName := "core.jar")
lazy val demoSetup = project.dependsOn(core % "test->compile;test->test;compile->compile").
  settings(commonSettings: _*).
  settings(assemblyJarName := "setup.jar")
lazy val processingUnit = project.
  dependsOn(core).
  settings(commonSettings).
  settings(assemblyJarName := "demoPU.jar")
lazy val sparkJobs = project.
  dependsOn(core % "test->compile;test->test;compile->compile").
  settings(commonSettings).
  settings(assemblyJarName := "sparkjobs.jar")
lazy val web = project.
  dependsOn(core).
  settings(commonSettings).
  disablePlugins(sbtassembly.AssemblyPlugin)
  
lazy val root = (project in file(".")).
  settings(commonSettings, organization := orgPrefix, name := coreName)
  .aggregate(core, demoSetup, processingUnit, sparkJobs, web)