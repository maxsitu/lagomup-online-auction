package com.example.auction.transaction.protocol

import java.util.UUID

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.{Done, NotUsed}
import com.example.auction.item.api.{Item, ItemEvent, ItemService, ItemSummaryPagingState}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.testkit.ProducerStubFactory

class ItemServiceStub(actorSystem: ActorSystem, materializer: Materializer) extends ItemService {

  val producerStub = new ProducerStubFactory(actorSystem, materializer).producer[ItemEvent]("item-ItemEvent")

  def createItem(): ServiceCall[Item, Item] = ???

  def startAuction(id: UUID): ServiceCall[NotUsed, Done] = ???

  def getItem(id: UUID): ServiceCall[NotUsed, Item] = ???

  def getItemsForUser(id: UUID, status: String, page: Option[String]): ServiceCall[NotUsed, ItemSummaryPagingState] = ???

  def itemEvents: Topic[ItemEvent] = producerStub.topic

}
