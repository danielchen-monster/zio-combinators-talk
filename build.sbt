name := "zio-examples"
version := "0.1"
addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3")
scalaVersion := "2.13.3"

libraryDependencies ++= Seq("dev.zio" %% "zio", "dev.zio" %% "zio-streams").map(_ % "1.0.3")
libraryDependencies += "dev.zio" %% "zio-interop-reactivestreams" % "1.0.3.5"
libraryDependencies += "software.amazon.awssdk" % "dynamodb" % "2.15.23"

// https://mvnrepository.com/artifact/io.projectreactor/reactor-core
libraryDependencies += "io.projectreactor" % "reactor-core" % "3.4.0"

