package com.example.auction.bidding.protocol


import com.example.auction.bidding.api._
import com.example.auction.bidding.impl.BiddingPorts
import akka.stream.scaladsl.Source
import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.lightbend.lagom.scaladsl.pubsub.{PubSubRegistry, TopicId}
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import scala.concurrent.{ExecutionContext, Future}
import java.util.UUID

trait BiddingServiceCalls_TODO {

  val ports: BiddingPorts

  def _placeBid(itemId: UUID, userId: UUID, request: PlaceBid): Future[BidResult] = {
  ???
}

def _getBids(itemId: UUID, request: NotUsed): Future[List[Bid]] = {
  ???
}



  def _placeBidAuthentication[Request, Response](serviceCall: UUID => ServerServiceCall[Request, Response]): ServerServiceCall[Request, Response] = {
  ???
}



}

