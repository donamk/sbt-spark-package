// pulled from http://www.scala-sbt.org/0.13/docs/Testing-sbt-plugins.html

sys.props.get("plugin.version") match {
  case Some(x) => addSbtPlugin("org.spark-packages" % "sbt-spark-package" % x)
  case _ => sys.error("""|The system property 'plugin.version' is not defined.
                         |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
}
