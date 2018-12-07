package com.example.auction.bidding.impl


import com.example.auction.bidding.api._
import com.example.auction.bidding.protocol._
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.pubsub.PubSubRegistry
import akka.stream.scaladsl.Flow
import scala.concurrent.ExecutionContext


class BiddingServiceImpl(val pubSubRegistry: PubSubRegistry)
                                         (implicit val ec: ExecutionContext)  extends BiddingService
  with BiddingServiceCalls   {

  
  override def placeBid() = ServiceCall { request =>
  _placeBid(request)
}

override def getBids() = ServiceCall { request =>
  _getBids(request)
}


  

}

