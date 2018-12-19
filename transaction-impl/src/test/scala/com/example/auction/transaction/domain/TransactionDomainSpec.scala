package com.example.auction.transaction.domain

import java.time.Duration
import java.util.UUID

import akka.actor.ActorSystem
import com.example.auction.transaction.impl._
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.testkit.PersistentEntityTestDriver
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class TransactionDomainSpec extends WordSpec with Matchers with BeforeAndAfterAll with MockFactory {

  val system = ActorSystem("TransactionDomainSpec", JsonSerializerRegistry.actorSystemSetupFor(TransactionSerializerRegistry))

  val itemId = UUID.randomUUID()
  val creator = UUID.randomUUID()
  val winner = UUID.randomUUID()
  val itemData = ItemData("title", "desc", "EUR", 1, 10, 10, None)
  val deliveryData = DeliveryData("Addr1", "Addr2", "City", "State", 27, "Country")
  val deliveryPrice = 500
  val payment = Payment("Payment sent via wire transfer")

  val transactionAggregate = TransactionAggregate(itemId, creator, winner, itemData, 2000, None, None, None)
  val startTransaction = StartTransaction(transactionAggregate)
  val submitDeliveryDetails = SubmitDeliveryDetails(winner, deliveryData)

  def withTestDriver(block: PersistentEntityTestDriver[TransactionCommand, TransactionEvent, TransactionState] => Unit): Unit = {
    val driver = new PersistentEntityTestDriver(system, new TransactionEntity(mock[TransactionEventStream]), itemId.toString)
    block(driver)
    if (driver.getAllIssues.nonEmpty) {
      driver.getAllIssues.foreach(println)
      fail(s"There were issues ${driver.getAllIssues.head}")
    }
  }

  "The transaction entity" should {

    "emit event when creating transaction" in withTestDriver { driver =>
      val outcome = driver.run(startTransaction)
      outcome.state.status should ===(TransactionAggregateStatus.NegotiatingDelivery)
      outcome.state.aggregate should ===(Some(transactionAggregate))
      outcome.events should contain only TransactionStarted(itemId, transactionAggregate)
    }

    "emit event when submitting delivery details" in withTestDriver { driver =>
      driver.run(startTransaction)
      val outcome = driver.run(submitDeliveryDetails)
      outcome.state.status should ===(TransactionAggregateStatus.NegotiatingDelivery)
      outcome.events should contain only DeliveryDetailsSubmitted(itemId, deliveryData)
    }

  }

}
