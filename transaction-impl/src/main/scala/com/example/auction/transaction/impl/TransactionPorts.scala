package com.example.auction.transaction.impl

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.example.auction.item.api._



import scala.concurrent.ExecutionContext

case class TransactionPorts(
  akkaComponents: AkkaComponents, 
itemService: ItemService
)

case class AkkaComponents(
  actorSystem: ActorSystem,
  mat: Materializer,
  ec: ExecutionContext
)

