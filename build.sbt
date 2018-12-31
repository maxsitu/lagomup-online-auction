organization in ThisBuild := "com.example.auction"
version in ThisBuild := "0.1-SNAPSHOT"

scalaVersion in ThisBuild := "2.12.7"

lazy val `online-auction` = (project in file("."))
  .aggregate(
    `utils`,
    `bidding-api`, `bidding-impl`,
    `item-api`, `item-impl`,
    `user-api`, `user-impl`,
    `transaction-api`, `transaction-impl`
  )

lazy val `utils` = (project in file("utils"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      lagomScaladslServer % Optional,
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
  .settings(lagomForkedTestSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.softwaremill.macwire" %% "macros" % "2.3.1" % Provided,
      lagomScaladslPubSub,
      "org.scalatest" %% "scalatest" % "3.0.5" % Test,
      "org.scalamock" %% "scalamock" % "4.1.0" % Test,
      lagomScaladslTestKit,
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
  .dependsOn(`utils`, `bidding-api`)

lazy val `item-impl` = (project in file("item-impl"))
  .enablePlugins(LagomScala)
  .settings(lagomForkedTestSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.softwaremill.macwire" %% "macros" % "2.3.1" % Provided,
      lagomScaladslPubSub,
      "org.scalatest" %% "scalatest" % "3.0.5" % Test,
      "org.scalamock" %% "scalamock" % "4.1.0" % Test,
      lagomScaladslTestKit,
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker
    )
  )
  .dependsOn(`item-api`)

lazy val `user-api` = (project in file("user-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
  .dependsOn(`utils`)

lazy val `user-impl` = (project in file("user-impl"))
  .enablePlugins(LagomScala)
  .settings(lagomForkedTestSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.softwaremill.macwire" %% "macros" % "2.3.1" % Provided,
      lagomScaladslPubSub,
      "org.scalatest" %% "scalatest" % "3.0.5" % Test,
      "org.scalamock" %% "scalamock" % "4.1.0" % Test,
      lagomScaladslTestKit,
      lagomScaladslPersistenceCassandra
    )
  )
  .dependsOn(`user-api`)

lazy val `transaction-api` = (project in file("transaction-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )
  .dependsOn(`utils`, `item-api`)

lazy val `transaction-impl` = (project in file("transaction-impl"))
  .enablePlugins(LagomScala)
  .settings(lagomForkedTestSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.softwaremill.macwire" %% "macros" % "2.3.1" % Provided,
      lagomScaladslPubSub,
      "org.scalatest" %% "scalatest" % "3.0.5" % Test,
      "org.scalamock" %% "scalamock" % "4.1.0" % Test,
      lagomScaladslTestKit,
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaClient
    )
  )
  .dependsOn(`transaction-api`)

