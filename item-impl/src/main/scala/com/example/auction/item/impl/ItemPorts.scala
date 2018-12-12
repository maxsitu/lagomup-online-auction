package com.example.auction.item.impl

import akka.actor.ActorSystem
import akka.stream.Materializer

import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession

import com.lightbend.lagom.scaladsl.pubsub.PubSubRegistry
import scala.concurrent.ExecutionContext

case class ItemPorts(
  pubSubRegistry: PubSubRegistry, 
actorSystem: ActorSystem, 
mat: Materializer, 
ec: ExecutionContext, 
entityRegistry: PersistentEntityRegistry, 
db: CassandraSession
)

