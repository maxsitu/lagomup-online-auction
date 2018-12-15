package com.example.auction.bidding.domain

import com.example.auction.bidding.impl._
import akka.NotUsed
import akka.stream.scaladsl.Source
import com.datastax.driver.core.Row
import scala.concurrent.Future
import java.util.UUID

trait BiddingReadRepository_TODO {

  def endedAuctions(endAuction: Long): Source[String, NotUsed]



  def mapEndedAuctionsResult(row: Row): String = {
  ???
}



}

