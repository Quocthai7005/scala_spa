name := "spa_drduong"
 
version := "1.0" 
      
lazy val `spa_drduong` = (project in file(".")).enablePlugins(PlayScala)



libraryDependencies += guice

libraryDependencies += evolutions

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
      
scalaVersion := "2.12.2"

libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test , guice,
  "mysql" % "mysql-connector-java" % "5.1.35",
  "org.playframework.anorm" %% "anorm" % "2.6.1",
  "com.adrianhurt" %% "play-bootstrap" % "1.4-P26-B4-SNAPSHOT",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

PlayKeys.devSettings := Seq("play.akka.dev-mode.akka.http.parsing.max-uri-length" -> "1000000")
PlayKeys.devSettings ++= Seq("play.akka.dev-mode.akka.http.parsing.max-header-value-length" -> "1000000 bytes")

      