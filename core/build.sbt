//version := "1.0.0-SNAPSHOT"
val insightEdgeVersion = "1.0.0"
val xapVersion = "12.0.0"
val springVersion = "4.1.1.RELEASE"
//scalaVersion := "2.10.6"
resolvers += Resolver.mavenLocal
resolvers += "Openspaces Maven Repository" at "http://maven-repository.openspaces.org"

libraryDependencies ++= Seq(
  "org.gigaspaces.insightedge" % "insightedge-core" % insightEdgeVersion % "provided" exclude("javax.jms", "jms") exclude("org.gigaspaces", "xap-openspaces") exclude("org.gigaspaces", "xap-spatial"),
  "org.gigaspaces.insightedge" % "insightedge-scala" % insightEdgeVersion % "provided" exclude("javax.jms", "jms") exclude("org.gigaspaces", "xap-openspaces") exclude("org.gigaspaces", "xap-spatial"),
  "org.gigaspaces" % "xap-openspaces" % xapVersion % "provided" exclude("javax.jms", "jms"),
  "org.gigaspaces" % "xap-common" % xapVersion % "provided" exclude("javax.jms", "jms"),
  "org.gigaspaces" % "xap-datagrid" % xapVersion % "provided" exclude("javax.jms", "jms"),
  "org.springframework" % "spring-core" % springVersion % "provided",
  "org.springframework" % "spring-tx" % springVersion % "provided",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)
