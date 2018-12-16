package com.example.auction.bidding.impl

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

import akka.Done
import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.example.auction.bidding.domain.BidValidationException
import com.example.auction.bidding.domain.BiddingDomain._
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.pubsub.PubSubRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, Inside, Matchers, WordSpec}

class BiddingEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll with Inside with MockFactory {

  val system = ActorSystem("BiddingEntitySpec", JsonSerializerRegistry.actorSystemSetupFor(BiddingSerializerRegistry))

  val itemId = UUID.randomUUID
  val creator = UUID.randomUUID
  val bidder1 = UUID.randomUUID
  val bidder2 = UUID.randomUUID
  val auction = Auction(itemId, creator, 2000, 50, Instant.now, Instant.now.plus(7, ChronoUnit.DAYS))

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  def withTestDriver(block: PersistentEntityTestDriver[BiddingCommand, BiddingEvent, BiddingState] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new BiddingEntity(mock[BiddingEventStream]), itemId.toString)
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
      driver.run(StartAuction(auction), PlaceBid(3000, bidder1))
      val outcome = driver.run(PlaceBid(2500, bidder2))
      outcome.events.map(matchable) should contain inOrderOnly(
        MatchingBid(bidder2, 2500, 2500),
        MatchingBid(bidder1, 2550, 3000)
      )
      outcome.state.aggregate.get.biddingHistory.map(matchable) should contain inOrderOnly(
        MatchingBid(bidder1, 2550, 3000),
        MatchingBid(bidder2, 2500, 2500),
        MatchingBid(bidder1, 2000, 3000)
      )
      outcome.replies should contain only PlaceBidResult(AcceptedOutbidStatus, 2550, Some(bidder1))
    }

    "accept a bid equal to the current maximum" in withTestDriver { driver =>
      driver.run(StartAuction(auction), PlaceBid(3000, bidder1))
      val outcome = driver.run(PlaceBid(3000, bidder2))
      outcome.events.map(matchable) should contain inOrderOnly(
        MatchingBid(bidder2, 2950, 3000),
        MatchingBid(bidder1, 3000, 3000)
      )
      outcome.state.aggregate.get.biddingHistory.map(matchable) should contain inOrderOnly(
        MatchingBid(bidder1, 3000, 3000),
        MatchingBid(bidder2, 2950, 3000),
        MatchingBid(bidder1, 2000, 3000)
      )
      outcome.replies should contain only PlaceBidResult(AcceptedOutbidStatus, 3000, Some(bidder1))
    }

    "accept a bid over the current maximum" in withTestDriver { driver =>
      driver.run(StartAuction(auction), PlaceBid(3000, bidder1))
      val outcome = driver.run(PlaceBid(4000, bidder2))
      outcome.events.map(matchable) should contain only
        MatchingBid(bidder2, 3050, 4000)
      outcome.state.aggregate.get.biddingHistory.map(matchable) should contain inOrderOnly(
        MatchingBid(bidder2, 3050, 4000),
        MatchingBid(bidder1, 2000, 3000)
      )
      outcome.replies should contain only PlaceBidResult(AcceptedStatus, 3050, Some(bidder2))
    }

    "accept a bid less than the increment over the current maximum" in withTestDriver { driver =>
      driver.run(StartAuction(auction), PlaceBid(3000, bidder1))
      val outcome = driver.run(PlaceBid(3020, bidder2))
      outcome.events.map(matchable) should contain only
        MatchingBid(bidder2, 3020, 3020)
      outcome.state.aggregate.get.biddingHistory.map(matchable) should contain inOrderOnly(
        MatchingBid(bidder2, 3020, 3020),
        MatchingBid(bidder1, 2000, 3000)
      )
      outcome.replies should contain only PlaceBidResult(AcceptedStatus, 3020, Some(bidder2))
    }

    "reject a bid less than the current bid" in withTestDriver { driver =>
      driver.run(StartAuction(auction), PlaceBid(3000, bidder1), PlaceBid(2500, bidder2))
      val outcome = driver.run(PlaceBid(2520, bidder2))
      outcome.events shouldBe empty
      outcome.state.aggregate.get.biddingHistory should have size 3
      outcome.replies should contain only PlaceBidResult(TooLowStatus, 2550, Some(bidder1))
    }

    "reject a bid less than the incement over the current bid" in withTestDriver { driver =>
      driver.run(StartAuction(auction), PlaceBid(3000, bidder1), PlaceBid(2500, bidder2))
      val outcome = driver.run(PlaceBid(2570, bidder2))
      outcome.events shouldBe empty
      outcome.state.aggregate.get.biddingHistory should have size 3
      outcome.replies should contain only PlaceBidResult(TooLowStatus, 2550, Some(bidder1))
    }

    "accept a bid equal to the incement over the current bid" in withTestDriver { driver =>
      driver.run(StartAuction(auction), PlaceBid(3000, bidder1), PlaceBid(2500, bidder2))
      val outcome = driver.run(PlaceBid(2600, bidder2))
      outcome.events.map(matchable) should contain inOrderOnly(
        MatchingBid(bidder2, 2600, 2600),
        MatchingBid(bidder1, 2650, 3000)
      )
      outcome.state.aggregate.get.biddingHistory.map(matchable) should contain inOrderOnly(
        MatchingBid(bidder1, 2650, 3000),
        MatchingBid(bidder2, 2600, 2600),
        MatchingBid(bidder1, 2550, 3000),
        MatchingBid(bidder2, 2500, 2500),
        MatchingBid(bidder1, 2000, 3000)
      )
      outcome.replies should contain only PlaceBidResult(AcceptedOutbidStatus, 2650, Some(bidder1))
    }

    "reject a bid from the seller" in withTestDriver { driver =>
      driver.run(StartAuction(auction))
      a[BidValidationException] should be thrownBy driver.run(PlaceBid(2500, creator))
    }

    "allow the current winning bidder to lower their maximum bid" in withTestDriver { driver =>
      driver.run(StartAuction(auction), PlaceBid(3000, bidder1))
      val outcome = driver.run(PlaceBid(2500, bidder1))
      outcome.events.map(matchable) should contain only
        MatchingBid(bidder1, 2000, 2500)
      outcome.state.aggregate.get.biddingHistory.map(matchable) should contain only
        MatchingBid(bidder1, 2000, 2500)
      outcome.replies should contain only PlaceBidResult(AcceptedStatus, 2000, Some(bidder1))

    }

    "allow the current winning bidder to lower their maximum bid to the current price" in withTestDriver { driver =>
      driver.run(StartAuction(auction), PlaceBid(3000, bidder1), PlaceBid(2500, bidder2))
      val outcome = driver.run(PlaceBid(2550, bidder1))
      outcome.events.map(matchable) should contain only MatchingBid(bidder1, 2550, 2550)
      outcome.state.aggregate.get.biddingHistory.map(matchable) should contain inOrderOnly(
        MatchingBid(bidder1, 2550, 2550),
        MatchingBid(bidder2, 2500, 2500),
        MatchingBid(bidder1, 2000, 3000)
      )
      outcome.replies should contain only PlaceBidResult(AcceptedStatus, 2550, Some(bidder1))
    }

    "prevent the current from lowering their maximum bid to below the current price" in withTestDriver { driver =>
      driver.run(StartAuction(auction), PlaceBid(3000, bidder1), PlaceBid(2500, bidder2))
      val outcome = driver.run(PlaceBid(2400, bidder1))
      outcome.events shouldBe empty
      outcome.state.aggregate.get.biddingHistory should have size 3
      outcome.replies should contain only PlaceBidResult(TooLowStatus, 2550, Some(bidder1))
    }

    "allow the current winning bidder to raise their bid" in withTestDriver { driver =>
      driver.run(StartAuction(auction), PlaceBid(3000, bidder1))
      val outcome = driver.run(PlaceBid(3500, bidder1))
      outcome.events.map(matchable) should contain only MatchingBid(bidder1, 2000, 3500)
      outcome.state.aggregate.get.biddingHistory.map(matchable) should contain only MatchingBid(bidder1, 2000, 3500)
      outcome.replies should contain only PlaceBidResult(AcceptedStatus, 2000, Some(bidder1))
    }

    "prevent bidding after the auctions end time is up" in withTestDriver { driver =>
      val auction = Auction(itemId, creator, 2000, 50, Instant.now, Instant.now.minus(7, ChronoUnit.DAYS))
      driver.run(StartAuction(auction))
      val outcome = driver.run(PlaceBid(3000, bidder1))
      outcome.events shouldBe empty
      outcome.state.aggregate.get.biddingHistory shouldBe empty
      outcome.replies should contain only PlaceBidResult(FinishedStatus, 0, None)
    }

    "allow the auction to be finished" in withTestDriver { driver =>
      driver.run(StartAuction(auction), PlaceBid(3000, bidder1), PlaceBid(3500, bidder2))
      val outcome = driver.run(FinishBidding)
      outcome.events should contain only BiddingFinished
      outcome.state.status should ===(AuctionAggregateStatus.Complete)
    }

    "allow cancelling the auction" in withTestDriver { driver =>
      driver.run(StartAuction(auction), PlaceBid(3000, bidder1), PlaceBid(3500, bidder2))
      val outcome = driver.run(CancelAuction)
      outcome.events should contain only AuctionCancelled
      outcome.state.status should ===(AuctionAggregateStatus.Cancelled)
    }

    "allow cancelling the auction before started" in withTestDriver { driver =>
      val outcome1 = driver.run(CancelAuction)
      outcome1.events should contain only AuctionCancelled
      outcome1.state.status should ===(AuctionAggregateStatus.Cancelled)
      // You should not be able to start a cancelled auction
      val outcome2 = driver.run(StartAuction(auction))
      outcome2.events shouldBe empty
      outcome2.state.status should ===(AuctionAggregateStatus.Cancelled)
      outcome2.replies should contain only Done
    }

    "allow cancelling the auction after it's finished" in withTestDriver { driver =>
      driver.run(StartAuction(auction), PlaceBid(3000, bidder1), FinishBidding)
      val outcome = driver.run(CancelAuction)
      outcome.events should contain only AuctionCancelled
      outcome.state.status should ===(AuctionAggregateStatus.Cancelled)
    }

    "handle start auction idempotently" in withTestDriver { driver =>
      driver.run(StartAuction(auction), PlaceBid(3000, bidder1), PlaceBid(3500, bidder2))
      val outcome1 = driver.run(StartAuction(auction))
      outcome1.events shouldBe empty
      outcome1.state.status should ===(AuctionAggregateStatus.UnderAuction)
      driver.run(FinishBidding)
      val outcome2 = driver.run(StartAuction(auction))
      outcome2.events shouldBe empty
      outcome2.state.status should ===(AuctionAggregateStatus.Complete)
      driver.run(CancelAuction)
      val outcome3 = driver.run(StartAuction(auction))
      outcome3.events shouldBe empty
      outcome3.state.status should ===(AuctionAggregateStatus.Cancelled)
    }

    "handle finish auction idempotently" in withTestDriver { driver =>
      driver.run(StartAuction(auction), PlaceBid(3000, bidder1), PlaceBid(3500, bidder2), FinishBidding)
      val outcome1 = driver.run(FinishBidding)
      outcome1.events shouldBe empty
      outcome1.state.status should ===(AuctionAggregateStatus.Complete)
      driver.run(CancelAuction)
      val outcome2 = driver.run(FinishBidding)
      outcome2.events shouldBe empty
      outcome2.state.status should ===(AuctionAggregateStatus.Cancelled)
    }

    "handle cancel auction idempotently" in withTestDriver { driver =>
      driver.run(StartAuction(auction), PlaceBid(3000, bidder1), CancelAuction)
      val outcome = driver.run(CancelAuction)
      outcome.events shouldBe empty
      outcome.state.status should ===(AuctionAggregateStatus.Cancelled)
    }

    "not allow a bid to be placed before the auction has started" in withTestDriver { driver =>
      val outcome = driver.run(PlaceBid(3000, bidder1))
      outcome.events shouldBe empty
      outcome.state.status should ===(AuctionAggregateStatus.NotStarted)
      outcome.replies should contain only PlaceBidResult(NotStartedStatus, 0, None)
    }

    "not allow a bid to be placed when the auction is complete" in withTestDriver { driver =>
      driver.run(StartAuction(auction), PlaceBid(2500, bidder1), FinishBidding)
      val outcome = driver.run(PlaceBid(3000, bidder2))
      outcome.events shouldBe empty
      outcome.state.status should ===(AuctionAggregateStatus.Complete)
      outcome.state.aggregate.get.biddingHistory should have size 1
      outcome.replies should contain only PlaceBidResult(FinishedStatus, 2000, Some(bidder1))
    }

    "not allow a bid to be placed when the auction has been cancelled" in withTestDriver { driver =>
      driver.run(StartAuction(auction), PlaceBid(2500, bidder1), CancelAuction)
      val outcome = driver.run(PlaceBid(3000, bidder2))
      outcome.events shouldBe empty
      outcome.state.status should ===(AuctionAggregateStatus.Cancelled)
      outcome.state.aggregate.get.biddingHistory should have size 1
      outcome.replies should contain only PlaceBidResult(CancelledStatus, 2000, Some(bidder1))
    }

    "allow getting the auction state" in withTestDriver { driver =>
      driver.run(StartAuction(auction), PlaceBid(3000, bidder1))
      val outcome = driver.run(GetAuction)
      outcome.events shouldBe empty
      outcome.replies should contain only outcome.state.aggregate
    }


  }

  private case class MatchingBid(bidder: UUID, bidPrice: Int, maximumBid: Int)

  private def matchable(bid: Bid): MatchingBid = MatchingBid(bid.bidder, bid.bidPrice, bid.maximumBid)

  private def matchable(event: BiddingEvent): MatchingBid = event match {
    case BidPlaced(bid) => matchable(bid)
    case _ => ??? // TODO: Not specified in example
  }

}
