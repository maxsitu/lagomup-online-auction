package com.example.auction.bidding.impl

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.example.auction.bidding.domain.BiddingDomain._
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.pubsub.PubSubRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, Inside, Matchers, WordSpec}

class BiddingEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll with Inside with MockFactory {

  val system = ActorSystem("BiddingEntitySpec", JsonSerializerRegistry.actorSystemSetupFor(BiddingSerializerRegistry))

  val itemId = UUID.randomUUID.toString
  val creator = UUID.randomUUID.toString
  val bidder1 = UUID.randomUUID.toString
  val bidder2 = UUID.randomUUID.toString
  val auction = Auction(itemId, creator, 2000, 50, Instant.now, Instant.now.plus(7, ChronoUnit.DAYS))

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  def withTestDriver(block: PersistentEntityTestDriver[BiddingCommand, BiddingEvent, BiddingState] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new BiddingEntity(mock[PubSubRegistry]), itemId)
    block(driver)
    if (driver.getAllIssues.nonEmpty) {
      driver.getAllIssues.foreach(println)
      fail("There were issues: " + driver.getAllIssues.head)
    }
  }

  "The auction entity" should {

    "allow starting an auction" in withTestDriver { driver =>
      val outcome = driver.run(StartAuction(auction))
      outcome.state.status should ===(AuctionAggregateStatus.UnderAuction)
      outcome.state.aggregate.get.auction should ===(auction)
      outcome.events should contain only AuctionStarted(auction)
    }

    "accept a first bid under the reserve" in withTestDriver { driver =>
      driver.run(StartAuction(auction))
      val outcome = driver.run(PlaceBid(500, bidder1))
      outcome.state.aggregate.get.biddingHistory.map(matchable) should contain only
        MatchingBid(bidder1, 500, 500)
      outcome.events.map(matchable) should contain only
        MatchingBid(bidder1, 500, 500)
      outcome.replies should contain only PlaceBidResult(AcceptedBelowReserveStatus, 500, Some(bidder1))
    }

    "accept a first bid with zero reserve" in withTestDriver { driver =>
      driver.run(StartAuction(auction.copy(reservePrice = 0)))
      val outcome = driver.run(PlaceBid(2000, bidder1))
      outcome.state.aggregate.get.biddingHistory.map(matchable) should contain only
        MatchingBid(bidder1, 50, 2000)
      outcome.events.map(matchable) should contain only
        MatchingBid(bidder1, 50, 2000)
      outcome.replies should contain only PlaceBidResult(AcceptedStatus, 50, Some(bidder1))
    }

    "accept a first bid equal to the reserve" in withTestDriver { driver =>
      driver.run(StartAuction(auction))
      val outcome = driver.run(PlaceBid(2000, bidder1))
      outcome.state.aggregate.get.biddingHistory.map(matchable) should contain only
        MatchingBid(bidder1, 2000, 2000)
      outcome.events.map(matchable) should contain only
        MatchingBid(bidder1, 2000, 2000)
      outcome.replies should contain only PlaceBidResult(AcceptedStatus, 2000, Some(bidder1))
    }

    "accept a bid under the current maximum" in withTestDriver { driver =>
      pending
    }

  }

  private case class MatchingBid(bidder: String, bidPrice: Int, maximumBid: Int)

  private def matchable(bid: Bid): MatchingBid = MatchingBid(bid.bidder, bid.bidPrice, bid.maximumBid)

  private def matchable(event: BiddingEvent): MatchingBid = event match {
    case BidPlaced(bid) => matchable(bid)
  }


}
