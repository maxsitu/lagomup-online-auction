package com.example.auction.bidding.protocol


import com.example.auction.bidding.api
import com.example.auction.bidding.impl._
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import scala.concurrent.{ExecutionContext, Future}

trait BiddingTopics_TODO {
  implicit val ec: ExecutionContext
  
  
  val entityRegistry: PersistentEntityRegistry

  def _bidEvents(): Topic[api.BidEvent] = {
  ???
}



}

