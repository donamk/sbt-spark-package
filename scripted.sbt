// created from http://www.scala-sbt.org/0.13/docs/Testing-sbt-plugins.html

ScriptedPlugin.scriptedSettings

scriptedLaunchOpts := { scriptedLaunchOpts.value ++
  Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
}

scriptedBufferLog := false
