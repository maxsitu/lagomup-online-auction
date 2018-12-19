package com.example.auction.transaction.impl

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.example.auction.item.api._

import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession


import scala.concurrent.ExecutionContext

case class TransactionPorts(
  akkaComponents: AkkaComponents, 
itemService: ItemService, 
entityRegistry: PersistentEntityRegistry, 
db: CassandraSession, 
transactionEventStream: TransactionEventStream
)

case class AkkaComponents(
  actorSystem: ActorSystem,
  mat: Materializer,
  ec: ExecutionContext
)

