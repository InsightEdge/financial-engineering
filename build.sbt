
val insightEdgeVersion = "1.0.0"
val xapVersion = "12.0.1"

resolvers += Resolver.mavenLocal
resolvers += "Openspaces Maven Repository" at "http://maven-repository.openspaces.org"

logLevel := sbt.Level.Debug

lazy val commonSettings = Seq(
  scalaVersion := "2.10.6",
  version := "1.0.0"
)

lazy val core = project
lazy val setup = project.dependsOn(core)
lazy val processingUnit = project.dependsOn(core)
lazy val spark = project.dependsOn(core, setup)
lazy val web = project

lazy val root = (project in file(".")).
  settings(
    commonSettings,
    organization := "org.insightedge.examples",
    name := "financial-engineering"
  ).aggregate(core,setup,processingUnit,spark,web)

libraryDependencies ++= Seq(
  "org.gigaspaces" % "xap-openspaces" % xapVersion % "provided" exclude("javax.jms", "jms"),
  "org.gigaspaces" % "xap-common" % xapVersion % "provided" exclude("javax.jms", "jms"),
  "org.gigaspaces" % "xap-datagrid" % xapVersion % "provided" exclude("javax.jms", "jms")
)

//assemblyOutputPath in assembly := new File(s"target/financial-engineering.jar")
//
//assemblyMergeStrategy in assembly := {
//  case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class") => MergeStrategy.first
//  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
//  case PathList("META-INF", "license", "*") => MergeStrategy.discard
//  case inf if inf.startsWith("META-INF/") =>
//    inf.slice("META-INF/".length, inf.length).toLowerCase match{
//      case "manifest.mf" => MergeStrategy.discard
//      case n if n.endsWith(".sf") || n.endsWith(".dsa") || n.endsWith(".rsa") =>
//        MergeStrategy.discard
//      case n if n.endsWith("license") || n.endsWith("LICENSE") =>
//        MergeStrategy.discard
//      case _ => MergeStrategy.first
//    }
//  case _ => MergeStrategy.first
//}
//
//assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = true)

