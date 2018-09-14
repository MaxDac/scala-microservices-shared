
organization := "com.eng.db"

name := "eng.db.shared"

version := "0.1"

scalaVersion := "2.12.6"

//lazy val projectDir = System.getProperty("user.dir")

lazy val projectDir = sys.props.get("user.dir")

lazy val baseDir = {
    val os = sys.props.get("os.name")
    val localDir = projectDir
    
    val returnValue = (os, localDir) match {
        case (Some(operativeSystem), Some(dir)) if operativeSystem.contains("Windows") => s"file:${dir.substring(0, dir.lastIndexOf('\\'))}"
        case (_, Some(dir)) => s"file://${dir.substring(0, dir.lastIndexOf('/'))}"
        case _ => throw new Exception()
    }
    
    returnValue
}

assemblyOutputPath in assembly := new sbt.File(s"${projectDir.get}/out/artifacts/eng.db.shared.jar")

logLevel in assembly := util.Level.Error

assemblyMergeStrategy in assembly := {
    case "application.conf" => MergeStrategy.concat
    case "reference.conf"=> MergeStrategy.concat
    case PathList("META-INF", _ @ _*) => MergeStrategy.discard
    case _ => MergeStrategy.first
    //    case x =>
    //        val oldStrategy = (assemblyMergeStrategy in assembly).value
    //        oldStrategy(x)
}

fork in Test := true
javaOptions in Test += "-Dconfig.file=src/test/resources/defaults.conf"

libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-http-spray-json"   % "10.1.1",
    "com.typesafe.akka" %% "akka-http"   % "10.1.1",
    "com.typesafe.akka" %% "akka-stream" % "2.5.11",
    "com.typesafe.akka" %% "akka-http-testkit" % "10.1.1",
    "org.scalactic" %% "scalactic" % "3.0.5",
    "org.scalatest" %% "scalatest" % "3.0.5" % "test",
    "com.github.swagger-akka-http" %% "swagger-akka-http" % "0.14.0",
    "com.auth0" % "java-jwt" % "3.3.0",
    "org.scala-lang" % "scala-compiler" % "2.12.6",
    "org.scala-lang" % "scala-reflect" % "2.12.6",
    "com.rabbitmq" % "amqp-client" % "5.2.0",
    "com.typesafe" % "config" % "1.2.1",

    // Local dependencies
    "com.oracle" %% "oracle.jdbc" % "1.8" from s"$baseDir/jars/ojdbc8.jar",
    "com.oracle" %% "oracle.ucp" % "1.8" from s"$baseDir/jars/ucp.jar",
    "com.eng.db" %% "eng.db.dataaccess" % "0.1" from s"$baseDir/dataaccess/out/artifacts/eng.db.dataaccess.jar"
)