package com.example.auction.transaction.impl

import com.example.auction.transaction.api.TransactionService
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents

import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.pubsub.PubSubComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaClientComponents
import com.example.auction.item.api.ItemService

import com.softwaremill.macwire._

class TransactionApplicationLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new TransactionApplication(context) with LagomKafkaClientComponents with ItemEventSubscription {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new TransactionApplication(context) with LagomKafkaClientComponents with LagomDevModeComponents with ItemEventSubscription

  override def describeService = Some(readDescriptor[TransactionService])

}

abstract class TransactionApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
  with AhcWSComponents with PubSubComponents with CassandraPersistenceComponents {
  override lazy val jsonSerializerRegistry = TransactionSerializerRegistry
  persistentEntityRegistry.register(wire[TransactionEntity])
lazy val transactionEventStream = wire[TransactionEventStreamImpl]
lazy val transactionRepository = wire[TransactionRepositoryImpl]
readSide.register(transactionRepository)


  val itemService = serviceClient.implement[ItemService]

  lazy val akkaComponents = wire[AkkaComponents]
  lazy val ports = wire[TransactionPorts]
  override lazy val lagomServer = serverFor[TransactionService](wire[TransactionServiceImpl])
}

trait ItemEventSubscription {
  this: TransactionApplication =>

  val itemEventSubscriber = wire[ItemEventSubscriberImpl]

}
