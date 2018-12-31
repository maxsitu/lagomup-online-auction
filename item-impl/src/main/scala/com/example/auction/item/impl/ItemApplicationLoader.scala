package com.example.auction.item.impl

import com.example.auction.item.api.ItemService
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents

import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.pubsub.PubSubComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.example.auction.bidding.api.BiddingService

import com.softwaremill.macwire._

class ItemApplicationLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new ItemApplication(context) with LagomKafkaComponents {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new ItemApplication(context) with LagomKafkaComponents with LagomDevModeComponents

  override def describeService = Some(readDescriptor[ItemService])

}

abstract class ItemApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
  with AhcWSComponents with PubSubComponents with CassandraPersistenceComponents {
  override lazy val jsonSerializerRegistry = ItemSerializerRegistry
  persistentEntityRegistry.register(wire[ItemEntity])
  lazy val itemEventStream = wire[ItemEventStreamImpl]
  lazy val itemRepository = wire[ItemRepositoryImpl]
  readSide.register(itemRepository)

  val biddingService = serviceClient.implement[BiddingService]

  lazy val akkaComponents = wire[AkkaComponents]
  lazy val ports = wire[ItemPorts]
  override lazy val lagomServer = serverFor[ItemService](wire[ItemServiceImpl])
}

