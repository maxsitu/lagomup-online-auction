package com.example.auction.bidding.protocol


import com.example.auction.bidding.api
import com.example.auction.bidding.api._
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import scala.concurrent.Future

trait BiddingTopics_TODO {

  
  val entityRegistry: PersistentEntityRegistry

  def _bidEvents(): Topic[api.BidEvent] = {
  ???
}



}

