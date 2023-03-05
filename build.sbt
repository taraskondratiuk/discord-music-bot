ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "discord-music-bot",
    scalacOptions ++= Seq(
      "-Wconf:msg=While parsing annotations in:silent",
      "-Xfatal-warnings",
    ),
    resolvers ++= Seq(
      "m2-dv8tion"      at "https://m2.dv8tion.net/releases",
    ),
    libraryDependencies ++= Seq(
      "com.discord4j"               % "discord4j-core"                    % "3.2.3",
      "com.sedmelluq"               % "lavaplayer"                        % "1.3.77",
      "ch.qos.logback"              % "logback-classic"                   % "1.4.5",
      "com.typesafe.scala-logging"  %% "scala-logging"                    % "3.9.5",
    ).map(_ withSources()),
  )

