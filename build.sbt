lazy val root = (project in file("."))
  .settings(
    sbtPlugin := true,
    scalaVersion := "2.12.4",
    sbtVersion in Global := "1.0.3",
    name := "sbt-spark-package",
    organization := "org.spark-packages",
    description := "sbt plugin to develop, use, and publish Spark Packages",
    licenses := Seq(
      "Apache-2.0" -> url("http://opensource.org/licenses/Apache-2.0")),
    libraryDependencies ++= Seq(
      "org.scalaj" %% "scalaj-http" % "2.3.0",
      "org.apache.directory.studio" % "org.apache.commons.codec" % "1.8"
    ),
//    scalaCompilerBridgeSource := {
//      val sv = appConfiguration.value.provider.id.version
//      ("org.scala-sbt" % "compiler-interface" % sv % "component").sources
//    },
    scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false
  )

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.5")


