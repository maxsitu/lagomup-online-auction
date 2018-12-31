package com.example.auction.bidding.protocol

import com.example.auction.bidding.api._
import com.example.auction.bidding.impl.BiddingPorts
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import scala.concurrent.{ ExecutionContext, Future }

trait BiddingTopics_TODO {

  val ports: BiddingPorts

  def _bidEvents(): Topic[BidEvent] = {
    ???
  }

}

