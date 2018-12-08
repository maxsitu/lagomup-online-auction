package com.example.auction.bidding.impl

import com.example.auction.bidding.api.BiddingService
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator

import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.pubsub.PubSubComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents

import com.softwaremill.macwire._

class BiddingApplicationLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new BiddingApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new BiddingApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[BiddingService])

}

abstract class BiddingApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
  with AhcWSComponents with PubSubComponents  with LagomKafkaComponents {

  override lazy val lagomServer = serverFor[BiddingService](wire[BiddingServiceImpl])
  
  
  
  

}

