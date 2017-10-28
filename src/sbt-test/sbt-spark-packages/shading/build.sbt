
lazy val commonSettings = Seq(
  version := "0.1",
  name := "shading",
  scalaVersion := "2.10.6",
  organization := "great.test"
)

lazy val nonShadedDependencies = Seq(
  ModuleID("org.apache.commons", "commons-proxy", "1.0")
)

lazy val root = project.in(file("."))
  .settings(commonSettings)
  .aggregate(shade, distribute)


lazy val shade = Project("shaded", file("."))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= (Seq(
      "org.apache.commons" % "commons-weaver-antlib" % "1.2"
    ) ++ nonShadedDependencies.map(_ % "provided")),
    target := target.value / "shaded",
    assemblyShadeRules in assembly := Seq(
      ShadeRule.rename("org.apache.commons.**" -> "databricks.commons.@1").inAll
    )
  )

lazy val distribute = Project("distribution", file("."))
  .settings(commonSettings)
  .settings(
    spName := "test/shading",
    target := target.value / "distribution",
    spShade := true,
    assembly in spPackage := (assembly /*in shade*/).value,
    libraryDependencies ++= nonShadedDependencies,
    packageBin := {
      val shadedJar = (assembly /*in shade*/).value
      val targetJar = new File(target.value, name.value + "-" + version.value + ".jar")
      IO.move(shadedJar, targetJar)
      targetJar
    }
  )


TaskKey[Unit]("checkZip") := {
  IO.withTemporaryDirectory { dir =>
    IO.unzip(target.value / "shading-0.1.zip", dir)
    mustExist(dir / "shading-0.1.jar")
    jarContentChecks(dir / "shading-0.1.jar", python = true)
    validatePom(dir / "shading-0.1.pom", "test", "shading", Seq(
      "commons-proxy" -> true, "commons-weaver-antlib" -> false))
  }
}

def jarContentChecks(dir: File, python: Boolean): Unit = {
  IO.withTemporaryDirectory { jarDir =>
    IO.unzip(dir, jarDir)
    mustExist(jarDir / "Main.class")
    mustExist(jarDir / "setup.py", python)
    mustExist(jarDir / "simple" / "__init__.py", python)
    mustExist(jarDir / "requirements.txt", python)
    mustExist(jarDir / "databricks" / "commons" / "weaver" / "ant" / "WeaveTask.class", true)
    mustExist(jarDir / "databricks" / "commons" / "weaver" / "ant" / "CleanTask.class", true)
    mustExist(jarDir / "org" / "apache" / "commons" / "weaver" / "ant" / "WeaveTask.class", false)
    mustExist(jarDir / "org" / "apache" / "commons" / "weaver" / "ant" / "CleanTask.class", false)
    if (python) {
      mustContain(jarDir / "requirements.txt", Seq("databricks/spark-csv==0.1"))
    }
  }
}
def validatePom(file: File, groupId: String, artifactId: String, dependencies: Seq[(String, Boolean)]): Unit = {
  import scala.xml.XML
  mustExist(file)
  val pom = XML.loadFile(file)
  val pomArtifactIds = (pom \\ "artifactId").map(_.text).toSet
  dependencies.foreach { case (artifact, shouldExist) =>
    val exists = pomArtifactIds.contains(artifact)
    assert(exists == shouldExist, s"Test for artifact $artifact failed; Exists: $exists ShouldExist: $shouldExist in $pom")
  }
}
def mustContain(f: File, l: Seq[String]): Unit = {
  val lines = IO.readLines(f, IO.utf8)
  if (lines != l)
    throw new Exception("file " + f + " had wrong content:\n" + lines.mkString("\n") +
      "\n*** instead of ***\n" + l.mkString("\n"))
}
def mustExist(f: File, operator: Boolean = true): Unit = {
  if (operator) {
    if (!f.exists) sys.error("file " + f + " does not exist!")
  } else {
    if (f.exists) sys.error("file " + f + " does exist!")
  }
}
