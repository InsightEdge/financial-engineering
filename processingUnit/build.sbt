//version := "1.0.0-SNAPSHOT"
val insightEdgeVersion = "1.0.0"
val xapVersion = "12.0.1"
val springVersion = "4.1.1.RELEASE"
//scalaVersion := "2.10.6"
resolvers += Resolver.mavenLocal
resolvers += "Openspaces Maven Repository" at "http://maven-repository.openspaces.org"

libraryDependencies ++= Seq(
  "org.gigaspaces" % "xap-openspaces" % xapVersion % "provided" exclude("javax.jms", "jms"),
  "org.gigaspaces" % "xap-common" % xapVersion % "provided" exclude("javax.jms", "jms"),
  "org.gigaspaces" % "xap-datagrid" % xapVersion % "provided" exclude("javax.jms", "jms"),
  "org.springframework" % "spring-core" % springVersion % "provided",
  "org.springframework" % "spring-tx" % springVersion % "provided",
  "org.scalatest" %% "scalatest" % "2.0" % "test"
)
