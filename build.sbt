
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
  scalaVersion := "2.10.6"
)

lazy val core = project.settings(commonSettings)
lazy val demoSetup = project.dependsOn(core).settings(commonSettings)
lazy val processingUnit = project.dependsOn(core).settings(commonSettings)
lazy val sparkJobs = project.dependsOn(core, demoSetup).settings(commonSettings)
lazy val web = project.settings(commonSettings)

lazy val root = (project in file(".")).
  settings(commonSettings, organization := orgPrefix, name := coreName)
  .aggregate(core, demoSetup, processingUnit, sparkJobs, web)