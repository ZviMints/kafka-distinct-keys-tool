name := "kafka-distinct-keys-tool"
version := "0.1"
scalaVersion := "2.13.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream-kafka" % "2.1.1",
  "com.typesafe.akka" %% "akka-stream" % "2.6.15",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
)

// assembly
mainClass in assembly := Some("Main")
test in assembly := {}
assemblyJarName in assembly := s"kafka-distinct-keys-tool.jar"
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", _ @ _*) => MergeStrategy.discard
  case PathList("reference.conf")    => MergeStrategy.concat
  case _                        => MergeStrategy.first
}

resolvers ++= Seq(
  ("confluent".at("https://packages.confluent.io/maven/")),
  "DV Artifactory SBT".at("http://artifact.doubleverify.corp/artifactory/sbt/").withAllowInsecureProtocol(true),
  Resolver.defaultLocal,
  Resolver.mavenLocal,
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)