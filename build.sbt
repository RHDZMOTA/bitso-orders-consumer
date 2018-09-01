import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.rhdzmota",
      scalaVersion := "2.12.6",
      version      := "1.0.0"
    )),
    name := "bitso-orders-consumer",
    libraryDependencies ++= {
      val circeVersion = "0.9.3"
      val akkaVersion  = "2.5.13"
      val configVersion = "1.3.1"
      val akkaHttpVersion = "10.1.1"
      val cassandraAlpakkaVersion = "0.19"
      Seq(
        "com.typesafe" % "config" % configVersion,
        // Circe
        "io.circe" %% "circe-core"    % circeVersion,
        "io.circe" %% "circe-generic" % circeVersion,
        "io.circe" %% "circe-parser"  % circeVersion,
        // Akka Toolkit
        "com.typesafe.akka" %% "akka-actor"   % akkaVersion,
        "com.typesafe.akka" %% "akka-stream"  % akkaVersion,
        "com.typesafe.akka" %% "akka-http"    % akkaHttpVersion,
        "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
        // Cassandra
        "com.lightbend.akka" %% "akka-stream-alpakka-cassandra" % cassandraAlpakkaVersion,
        // Testing
        scalaTest % Test
      )
    },
    assemblyMergeStrategy in assembly := {
      case PathList("reference.conf")    => MergeStrategy.concat
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x => MergeStrategy.first
    }
  )
