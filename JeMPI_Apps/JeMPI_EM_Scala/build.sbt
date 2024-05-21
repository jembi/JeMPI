ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.14"

lazy val root = (project in file("."))
  .settings(
    name := "JeMPI_EM_Scala",
    scalacOptions += "-deprecation",
    libraryDependencies ++= Seq(
      // https://mvnrepository.com/artifact/org.scala-lang.modules/scala-parallel-collections
      "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.4",
      // https://mvnrepository.com/artifact/com.typesafe.scala-logging/scala-logging
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
      // https://mvnrepository.com/artifact/org.apache.kafka/kafka-streams
      "org.apache.kafka" % "kafka-streams" % "3.7.0",
      // https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients
      "org.apache.kafka" % "kafka-clients" % "3.7.0",
      // https://mvnrepository.com/artifact/org.apache.kafka/kafka-streams-scala
      "org.apache.kafka" %% "kafka-streams-scala" % "3.7.0",
      // https://mvnrepository.com/artifact/com.fasterxml.jackson.datatype/jackson-datatype-jsr310
      "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.17.1",
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.17.1",
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.17.1",
      // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
      "ch.qos.logback" % "logback-classic" % "1.5.6"
    ),
    assembly / assemblyJarName := "em-scala-fatjar-1.0.jar",
    assembly / assemblyMergeStrategy := {
      case x if Assembly.isConfigFile(x) => MergeStrategy.concat
      case PathList(ps @ _*)
          if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
        MergeStrategy.rename
      case PathList("META-INF", xs @ _*) =>
        (xs map {
          _.toLowerCase
        }) match {
          case ("manifest.mf" :: Nil) | ("index.list" :: Nil) |
              ("dependencies" :: Nil) =>
            MergeStrategy.discard
          case ps @ (x :: xs)
              if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
            MergeStrategy.discard
          case "plexus" :: xs =>
            MergeStrategy.discard
          case "services" :: xs =>
            MergeStrategy.filterDistinctLines
          case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
            MergeStrategy.filterDistinctLines
          case _ => MergeStrategy.first
        }
      case _ => MergeStrategy.first
    }
  )
