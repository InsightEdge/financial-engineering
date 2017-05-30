//version := "1.0.0-SNAPSHOT"
val insightEdgeVersion = "1.0.0"
val xapVersion = "12.0.0"
val springVersion = "4.1.1.RELEASE"
resolvers += Resolver.mavenLocal
resolvers += "Openspaces Maven Repository" at "http://maven-repository.openspaces.org"

fork in Test := true

javaOptions in Test := Seq("-Djava.rmi.server.hostname=127.0.0.1", "-Xmx1024M")

libraryDependencies ++= Seq(
  "org.gigaspaces" % "xap-openspaces" % xapVersion % "provided" exclude("javax.jms", "jms"),
  "org.gigaspaces" % "xap-common" % xapVersion % "provided" exclude("javax.jms", "jms"),
  "org.gigaspaces" % "xap-datagrid" % xapVersion % "provided" exclude("javax.jms", "jms"),
  "org.springframework" % "spring-core" % springVersion % "provided",
  "org.springframework" % "spring-tx" % springVersion % "provided",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "com.typesafe" % "config" % "1.2.1"
)
