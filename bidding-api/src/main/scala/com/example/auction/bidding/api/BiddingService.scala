package com.example.auction.bidding.api


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
import com.example.auction.utils.SecurityHeaderFilter

trait BiddingService extends Service {
  def placeBid(itemId: UUID): ServiceCall[PlaceBid, BidResult]

def getBids(itemId: UUID): ServiceCall[NotUsed, List[Bid]]


  def bidEvents: Topic[BidEvent]


  override def descriptor = {
  import Service._

  named("bidding")
    .withCalls(
  pathCall("/api/item/:id/bids", placeBid _)(implicitly[MessageSerializer[PlaceBid, ByteString]], implicitly[MessageSerializer[BidResult, ByteString]]),
pathCall("/api/item/:id/bids", getBids _)(implicitly[MessageSerializer[NotUsed, ByteString]], implicitly[MessageSerializer[List[Bid], ByteString]])
)

    .withTopics(
  topic("bidding-BidEvent", bidEvents _)(implicitly[MessageSerializer[BidEvent, ByteString]])
  .addProperty(
    KafkaProperties.partitionKeyStrategy,
    PartitionKeyStrategy[BidEvent](_.itemId.toString)
  )

)

    .withHeaderFilter(SecurityHeaderFilter.Composed)
    .withAutoAcl(true)
}

      
}

sealed trait BidEvent {
  val itemId: UUID
}

object BidEvent {
  implicit val format: Format[BidEvent] =
    derived.flat.oformat((__ \ "type").format[String])
}



case class BidPlaced(itemId: UUID, bid: Bid) extends BidEvent

object BidPlaced {
  implicit val format: Format[BidPlaced] = Json.format
}

case class BiddingFinished(itemId: UUID, winningBid: Option[Bid]) extends BidEvent

object BiddingFinished {
  implicit val format: Format[BiddingFinished] = Json.format
}



case class PlaceBid(maximumBidPrice: Int) 

object PlaceBid {
  implicit val format: Format[PlaceBid] = Json.format
}

case class BidResult(currentPrice: Int, status: String, currentBidder: Option[UUID]) 

object BidResult {
  implicit val format: Format[BidResult] = Json.format
}

case class Bid(bidder: UUID, bidTime: Instant, price: Int, maximumPrice: Int) 

object Bid {
  implicit val format: Format[Bid] = Json.format
}



