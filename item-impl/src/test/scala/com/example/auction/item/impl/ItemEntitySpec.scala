package com.example.auction.item.impl

import java.util.UUID

import akka.actor.ActorSystem
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.InvalidCommandException
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, OptionValues, WordSpec}

class ItemEntitySpec extends WordSpec with Matchers with BeforeAndAfterAll with OptionValues with MockFactory {

  private val system = ActorSystem("test", JsonSerializerRegistry.actorSystemSetupFor(ItemSerializerRegistry))

  private val itemId = UUID.randomUUID
  private val creatorId = UUID.randomUUID
  private val item = ItemAggregate(
    itemId, creatorId, "title", "desc", "EUR", 1, 10, None, ItemAggregateStatus.Created.toString, 10, None, None, None
  )

  private def withDriver[T](block: PersistentEntityTestDriver[ItemCommand, ItemEvent, ItemState] => T): T = {
    val driver = new PersistentEntityTestDriver(system, new ItemEntity(mock[ItemEventStream]), itemId.toString)
    try {
      block(driver)
    } finally {
      driver.getAllIssues shouldBe empty
    }
  }

  "The item entity" should {

    "allow creating an item" in withDriver { driver =>
      val outcome = driver.run(CreateItem(item))
      outcome.events should contain only ItemCreated(item)
      outcome.state.aggregate should ===(Some(item))
    }

    "allow starting an auction" in withDriver { driver =>
      driver.run(CreateItem(item))
      val outcome = driver.run(StartAuction(creatorId))
      val auctionStart = outcome.state.aggregate.value.auctionStart.value
      outcome.events should contain only AuctionStarted(auctionStart)
      outcome.state.status should ===(ItemAggregateStatus.Auction)
    }

    "only allow the creator to start an auction" in withDriver { driver =>
      driver.run(CreateItem(item))
      val outcome = driver.run(StartAuction(UUID.randomUUID))
      outcome.events shouldBe empty
      outcome.replies should have size 1
      outcome.replies.head shouldBe a[InvalidCommandException]
    }

    "ignore duplicate start auction commands" in withDriver { driver =>
      driver.run(CreateItem(item), StartAuction(creatorId))
      driver.run(StartAuction(creatorId)).events shouldBe empty
    }

    "allow updating the price" in withDriver { driver =>
      driver.run(CreateItem(item), StartAuction(creatorId))
      val outcome = driver.run(UpdatePrice(10))
      outcome.events should contain only PriceUpdated(10)
      outcome.state.aggregate.value.price.value should ===(10)
    }

    "allow finishing an auction" in withDriver { driver =>
      driver.run(CreateItem(item), StartAuction(creatorId))
      val winner = UUID.randomUUID
      val outcome = driver.run(FinishAuction(Some(winner), Some(20)))
      outcome.events should contain only AuctionFinished(Some(winner), Some(20))
      outcome.state.aggregate.value.auctionWinner.value should ===(winner)
      outcome.state.aggregate.value.price.value should ===(20)
      outcome.state.aggregate.value.status should ===(ItemAggregateStatus.Completed.toString)
    }

    "allow finishing an auction with no winner" in withDriver { driver =>
      driver.run(CreateItem(item), StartAuction(creatorId))
      val outcome = driver.run(FinishAuction(None, None))
      outcome.events should contain only AuctionFinished(None, None)
      outcome.state.aggregate.value.auctionWinner shouldBe empty
      outcome.state.aggregate.value.price shouldBe empty
      outcome.state.status should ===(ItemAggregateStatus.Completed)
    }

    "ignore a request to start a completed auction" in withDriver { driver =>
      driver.run(CreateItem(item), StartAuction(creatorId), FinishAuction(None, None))
      val outcome = driver.run(StartAuction(creatorId))
      outcome.events shouldBe empty
    }

    "allow getting an auction" in withDriver { driver =>
      driver.run(CreateItem(item))
      val outcome1 = driver.run(GetItem)
      outcome1.replies should contain only Some(item)
      driver.run(StartAuction(creatorId))
      val outcome2 = driver.run(GetItem)
      outcome2.replies should contain only outcome2.state.aggregate
      driver.run(FinishAuction(None, None))
      val outcome3 = driver.run(GetItem)
      outcome3.replies should contain only outcome3.state.aggregate
    }

  }

}
