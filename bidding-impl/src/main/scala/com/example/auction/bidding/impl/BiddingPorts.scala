package com.example.auction.bidding.impl

import akka.actor.ActorSystem
import akka.stream.Materializer

import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession

import com.example.auction.bidding.domain.BiddingReadRepository

import com.lightbend.lagom.scaladsl.pubsub.PubSubRegistry
import scala.concurrent.ExecutionContext

case class BiddingPorts(
  environment: Environment, 
pubSubRegistry: PubSubRegistry, 
entityRegistry: PersistentEntityRegistry, 
db: CassandraSession, 
biddingEventStream: BiddingEventStream, 
biddingRepository: BiddingReadRepository
)

case class Environment(
  actorSystem: ActorSystem,
  mat: Materializer,
  ec: ExecutionContext
)

