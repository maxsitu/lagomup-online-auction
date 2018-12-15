package com.example.auction.item.api


import akka.{Done, NotUsed}
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import akka.util.ByteString
import com.lightbend.lagom.scaladsl.api.deser.MessageSerializer
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import com.lightbend.lagom.scaladsl.playjson.JsonSerializer
import julienrf.json.derived
import play.api.libs.json._
import java.time.Instant
import java.util.UUID

trait ItemService extends Service {
  def createItem(): ServiceCall[Item, Item]

def startAuction(id: UUID): ServiceCall[NotUsed, Done]

def getItem(id: UUID): ServiceCall[NotUsed, Item]

def getItemForUser(id: UUID, status: String, page: Option[String]): ServiceCall[NotUsed, ItemSummaryPagingState]


  
  override def descriptor = {
  import Service._

  named("item")
    .withCalls(
  pathCall("/api/item", createItem _)(implicitly[MessageSerializer[Item, ByteString]], implicitly[MessageSerializer[Item, ByteString]]),
pathCall("/api/item/:id/start", startAuction _)(implicitly[MessageSerializer[NotUsed, ByteString]], implicitly[MessageSerializer[Done, ByteString]]),
pathCall("/api/item/:id", getItem _)(implicitly[MessageSerializer[NotUsed, ByteString]], implicitly[MessageSerializer[Item, ByteString]]),
pathCall("/api/item?userId&status&page", getItemForUser _)(implicitly[MessageSerializer[NotUsed, ByteString]], implicitly[MessageSerializer[ItemSummaryPagingState, ByteString]])
)

    
    .withAutoAcl(true)
}

      
}


case class Item(id: Option[UUID], creator: UUID, itemData: ItemData, price: Option[Int], status: String, auctionStart: Option[Instant], auctionEnd: Option[Instant], auctionWinner: Option[UUID]) 

object Item {
  implicit val format: Format[Item] = Json.format
}

case class ItemSummaryPagingState(items: List[ItemSummary], nextPage: String, count: Int) 

object ItemSummaryPagingState {
  implicit val format: Format[ItemSummaryPagingState] = Json.format
}

case class ItemData(title: String, description: String, currencyId: String, increment: Int, reservePrice: Int, auctionDuration: Int, categoryId: Option[UUID]) 

object ItemData {
  implicit val format: Format[ItemData] = Json.format
}

case class ItemSummary(id: UUID, title: String, currencyId: String, reservePrice: Int, status: String) 

object ItemSummary {
  implicit val format: Format[ItemSummary] = Json.format
}



