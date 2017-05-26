//version := "1.0.0-SNAPSHOT"
val insightEdgeVersion = "1.0.0"
val xapVersion = "12.0.1"
val springVersion = "4.1.1.RELEASE"
resolvers += Resolver.mavenLocal
resolvers += "Openspaces Maven Repository" at "http://maven-repository.openspaces.org"

fork in Test := true
parallelExecution in Test := false

javaOptions in Test := Seq("-Djava.rmi.server.hostname=127.0.0.1", "-Xmx1024M")

libraryDependencies ++= Seq(
  "org.gigaspaces.insightedge" % "insightedge-core" % insightEdgeVersion % "provided" exclude("javax.jms", "jms") exclude("org.gigaspaces", "xap-openspaces") exclude("org.gigaspaces", "xap-spatial"),
  "org.gigaspaces.insightedge" % "insightedge-scala" % insightEdgeVersion % "provided" exclude("javax.jms", "jms") exclude("org.gigaspaces", "xap-openspaces") exclude("org.gigaspaces", "xap-spatial"),
  "org.apache.spark" %% "spark-streaming-kafka" % "1.6.0" exclude("org.spark-project.spark", "unused"),
  "org.gigaspaces" % "xap-openspaces" % xapVersion % "provided" exclude("javax.jms", "jms"),
  "org.gigaspaces" % "xap-common" % xapVersion % "provided" exclude("javax.jms", "jms"),
  "org.gigaspaces" % "xap-datagrid" % xapVersion % "provided" exclude("javax.jms", "jms"),
  "org.springframework" % "spring-core" % springVersion % "provided",
  "org.springframework" % "spring-tx" % springVersion % "provided",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "net.manub" %% "scalatest-embedded-kafka" % "0.10.0" % "test",
  "com.holdenkarau" %% "spark-testing-base" % "1.6.0_0.6.0" % "test"
)
