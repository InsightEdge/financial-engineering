name := "financial-engineering"

version := "1.0.0-SNAPSHOT"

val xapVersion = "12.0.1"

scalaVersion := "2.10.4"

val insightEdgeVersion = "1.0.0"

resolvers += Resolver.mavenLocal

resolvers += "Openspaces Maven Repository" at "http://maven-repository.openspaces.org"

libraryDependencies ++= Seq(
  //  "org.gigaspaces.insightedge" % "insightedge-core" % insightEdgeVersion % "provided" exclude("javax.jms", "jms"),
  //  "org.gigaspaces.insightedge" % "insightedge-scala" % insightEdgeVersion % "provided" exclude("javax.jms", "jms"),
  "org.gigaspaces.insightedge" % "insightedge-core" % insightEdgeVersion exclude("javax.jms", "jms"),
  "org.gigaspaces.insightedge" % "insightedge-scala" % insightEdgeVersion exclude("javax.jms", "jms"),
  "org.gigaspaces" % "xap-openspaces" % xapVersion,
  //  "org.gigaspaces" % "xap-common" % xapVersion,
  //  "org.gigaspaces" % "xap-datagrid" % xapVersion,
  "org.apache.spark" %% "spark-streaming-kafka" % "1.6.0" % "provided",
  //  "org.scala-lang" %% "scala-library" % "2.10.6",
  "org.scalatest" %% "scalatest" % "2.0" % "test"
)

assemblyOutputPath in assembly := new File(s"target/financial-engineering.jar")

assemblyMergeStrategy in assembly := {
  case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class") => MergeStrategy.first
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case inf if inf.startsWith("META-INF/") =>
    inf.slice("META-INF/".size, inf.size).toLowerCase match{
      case "manifest.mf" => MergeStrategy.discard
      case n if n.endsWith(".sf") || n.endsWith(".dsa") || n.endsWith(".rsa") =>
        MergeStrategy.discard
      case _ => MergeStrategy.first
    }
  case _ => MergeStrategy.first
}

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = true)