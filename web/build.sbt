val xapVersion = "12.0.1"
val springVersion = "4.1.1.RELEASE"
val jacksonVersion = "2.3.0"

resolvers += Resolver.mavenLocal
resolvers += "Openspaces Maven Repository" at "http://maven-repository.openspaces.org"

libraryDependencies ++= Seq(
  "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided",
  "org.gigaspaces" % "xap-openspaces" % xapVersion % "provided" exclude("javax.jms", "jms"),
  "org.gigaspaces" % "xap-common" % xapVersion % "provided" exclude("javax.jms", "jms"),
  "org.gigaspaces" % "xap-datagrid" % xapVersion % "provided" exclude("javax.jms", "jms"),
  "org.springframework" % "spring-core" % springVersion % "provided",
  "org.springframework" % "spring-tx" % springVersion % "provided",
  "org.springframework" % "spring-web" % springVersion,
  "org.springframework" % "spring-webmvc" % springVersion,
  "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion,
  "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
  "org.gigaspaces" % "xap-jetty-8" % "12.1.1",
  "org.apache.commons" % "commons-math3" % "3.6.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)

enablePlugins(JettyPlugin)