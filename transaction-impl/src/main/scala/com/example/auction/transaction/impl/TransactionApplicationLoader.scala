package com.example.auction.transaction.impl

import com.example.auction.transaction.api.TransactionService
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator

import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.pubsub.PubSubComponents
import play.api.libs.ws.ahc.AhcWSComponents

import com.example.auction.item.api.ItemService

import com.softwaremill.macwire._

class TransactionApplicationLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new TransactionApplication(context)  {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new TransactionApplication(context)  with LagomDevModeComponents

  override def describeService = Some(readDescriptor[TransactionService])

}

abstract class TransactionApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
  with AhcWSComponents with PubSubComponents  {
  
  
  val itemService = serviceClient.implement[ItemService]

  lazy val akkaComponents = wire[AkkaComponents]
  lazy val ports = wire[TransactionPorts]
  override lazy val lagomServer = serverFor[TransactionService](wire[TransactionServiceImpl])
}

