package com.example.auction.user.impl

import com.example.auction.user.api.UserService
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents

import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.pubsub.PubSubComponents
import play.api.libs.ws.ahc.AhcWSComponents


import com.softwaremill.macwire._

class UserApplicationLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new UserApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new UserApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[UserService])

}

abstract class UserApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
  with AhcWSComponents with PubSubComponents with CassandraPersistenceComponents  {
  override lazy val jsonSerializerRegistry = UserSerializerRegistry
  persistentEntityRegistry.register(wire[UserEntity])
lazy val userEventStream = wire[UserEventStreamImpl]


  
  lazy val env = wire[Environment]
  lazy val ports = wire[UserPorts]
  override lazy val lagomServer = serverFor[UserService](wire[UserServiceImpl])
}

