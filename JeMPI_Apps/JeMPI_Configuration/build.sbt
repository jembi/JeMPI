ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.0"

// https://mvnrepository.com/artifact/org.scala-lang.modules/scala-parser-combinators
ThisBuild / libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1"

// https://mvnrepository.com/artifact/org.apache.commons/commons-text
ThisBuild / libraryDependencies += "org.apache.commons" % "commons-text" % "1.9"

// https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
ThisBuild / libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.13.3"

// https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-scala
ThisBuild / libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.3"

lazy val root = (project in file("."))
  .settings(
    name := "JeMPI_Configuration",
    idePackagePrefix := Some("org.jembi.jempi")
  )

