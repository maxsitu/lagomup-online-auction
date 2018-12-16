package com.example.auction.item.impl

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.example.auction.bidding.api._

import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession

import com.example.auction.item.domain.ItemReadRepository

import com.lightbend.lagom.scaladsl.pubsub.PubSubRegistry
import scala.concurrent.ExecutionContext

case class ItemPorts(
  akkaComponents: AkkaComponents, 
pubSubRegistry: PubSubRegistry, 
biddingService: BiddingService, 
entityRegistry: PersistentEntityRegistry, 
db: CassandraSession, 
itemEventStream: ItemEventStream, 
itemRepository: ItemReadRepository
)

case class AkkaComponents(
  actorSystem: ActorSystem,
  mat: Materializer,
  ec: ExecutionContext
)

