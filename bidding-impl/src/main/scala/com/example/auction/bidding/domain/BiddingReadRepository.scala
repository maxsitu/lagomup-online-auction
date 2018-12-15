package com.example.auction.bidding.domain

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.datastax.driver.core.Row

trait BiddingReadRepository {

  def endedAuctions(endAuction: Long): Source[String, NotUsed]

  def mapEndedAuctionsResult(row: Row): String = {
    row.getString("itemId")
  }

}

