organization in ThisBuild := "com.example.auction"
version in ThisBuild := "0.1-SNAPSHOT"

scalaVersion in ThisBuild := "2.12.7"

lazy val `online-auction` = (project in file("."))
  .aggregate(
    `utils`,
    `bidding-api`, `bidding-impl`,
`item-api`, `item-impl`
  )

lazy val `utils` = (project in file("utils"))
  .settings(
    libraryDependencies ++= Seq(
      "com.lightbend.lagom" %% "lagom-scaladsl-play-json" % "1.4.9",
"org.julienrf" %% "play-json-derived-codecs" % "4.0.0"
    )
  )

lazy val `bidding-api` = (project in file("bidding-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
  .dependsOn(`utils`)


lazy val `bidding-impl` = (project in file("bidding-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      "com.softwaremill.macwire" %% "macros" % "2.3.1" % Provided,
lagomScaladslPubSub,
lagomScaladslPersistenceCassandra,
lagomScaladslKafkaBroker
    )
  )
  .dependsOn(`bidding-api`)


lazy val `item-api` = (project in file("item-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
  .dependsOn(`utils`)


lazy val `item-impl` = (project in file("item-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      "com.softwaremill.macwire" %% "macros" % "2.3.1" % Provided,
lagomScaladslPubSub
    )
  )
  .dependsOn(`item-api`)


