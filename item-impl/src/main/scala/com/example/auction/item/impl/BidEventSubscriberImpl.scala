package com.example.auction.item.impl

import com.example.auction.bidding.api._

import akka.stream.scaladsl.Flow
import com.example.auction.item.protocol.BidEventSubscriber
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

class BidEventSubscriberImpl(biddingService: BiddingService, val entityRegistry: PersistentEntityRegistry) extends BidEventSubscriber {

  biddingService.bidEvents.subscribe.atLeastOnce(Flow[BidEvent].mapAsync(1) { e =>
    onBidEvent(e)
  })

}

