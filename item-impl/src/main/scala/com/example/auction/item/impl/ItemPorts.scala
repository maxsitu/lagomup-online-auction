package com.example.auction.item.impl

import akka.actor.ActorSystem
import akka.stream.Materializer

import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession

import com.example.auction.item.domain.ItemReadRepository

import com.lightbend.lagom.scaladsl.pubsub.PubSubRegistry
import scala.concurrent.ExecutionContext

case class ItemPorts(
  environment: Environment, 
pubSubRegistry: PubSubRegistry, 
entityRegistry: PersistentEntityRegistry, 
db: CassandraSession, 
itemEventStream: ItemEventStream, 
itemRepository: ItemReadRepository
)

case class Environment(
  actorSystem: ActorSystem,
  mat: Materializer,
  ec: ExecutionContext
)

