package com.example.auction.bidding.protocol


import com.example.auction.bidding.api
import com.example.auction.bidding.api._
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer

import scala.concurrent.Future

trait BiddingTopics {




  def _bidEvents(): Topic[api.BidEvent] = {
  ???
}



}

