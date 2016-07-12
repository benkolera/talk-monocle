import sbt._

val libraryVersion = "1.2.2"     // or "1.3.0-SNAPSHOT"

lazy val root = (project in file(".")).
  settings(
    name := "monocle-demo",
    scalaVersion := "2.11.8",
    libraryDependencies := Seq(
      "com.github.julien-truffaut"  %%  "monocle-core"    % libraryVersion,
      "com.github.julien-truffaut"  %%  "monocle-macro"   % libraryVersion
    )
  )

// for @Lenses macro support
addCompilerPlugin("org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.full)
