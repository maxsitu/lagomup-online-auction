package com.example.auction.transaction.domain

import java.util.UUID

import akka.actor.ActorSystem
import com.example.auction.transaction.impl._
import com.lightbend.lagom.scaladsl.api.transport.Forbidden
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
  val setDeliveryPrice = SetDeliveryPrice(creator, deliveryPrice)
  val approveDeliveryDetails = ApproveDeliveryDetails(creator)


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

    "forbid submitting delivery details by non-buyer" in withTestDriver { driver =>
      driver.run(startTransaction)
      val hacker = UUID.randomUUID()
      val invalid = SubmitDeliveryDetails(hacker, deliveryData)
      a[Forbidden] should be thrownBy driver.run(invalid)
    }

    "emit event when setting delivery price" in withTestDriver { driver =>
      driver.run(startTransaction)
      val outcome = driver.run(setDeliveryPrice)
      outcome.state.status should ===(TransactionAggregateStatus.NegotiatingDelivery)
      outcome.state.aggregate.get.deliveryPrice.get should ===(deliveryPrice)
      outcome.events should contain only DeliveryPriceUpdated(itemId, deliveryPrice)
    }

    "forbid setting delivery price by non-seller" in withTestDriver { driver =>
      driver.run(startTransaction)
      val hacker = UUID.randomUUID()
      val invalid = SetDeliveryPrice(hacker, deliveryPrice)
      a[Forbidden] should be thrownBy driver.run(invalid)
    }

    "emit event when approving delivery details" in withTestDriver { driver =>
      driver.run(startTransaction)
      driver.run(submitDeliveryDetails)
      driver.run(setDeliveryPrice)
      val outcome = driver.run(approveDeliveryDetails)
      outcome.state.status should ===(TransactionAggregateStatus.PaymentPending)
      outcome.events should contain only DeliveryDetailsApproved(itemId)
    }

    "forbid approve delivery details by non-seller" in withTestDriver { driver =>
      driver.run(startTransaction)
      driver.run(submitDeliveryDetails)
      driver.run(setDeliveryPrice)
      val hacker = UUID.randomUUID()
      val invalid = ApproveDeliveryDetails(hacker)
      a[Forbidden] should be thrownBy driver.run(invalid)
    }

    "forbid approve empty delivery details" in withTestDriver { driver =>
      driver.run(startTransaction)
      a[Forbidden] should be thrownBy driver.run(approveDeliveryDetails)
    }

    "emit event when submitting payment details" in withTestDriver { driver =>
      pending
    }

    "forbid submitting payment details by non-buyer" in withTestDriver { driver =>
      pending
    }

    "emit event when approving payment" in withTestDriver { driver =>
      pending
    }

    "emit event when rejecting payment" in withTestDriver { driver =>
      pending
    }

    "forbid submit payment status for non-seller" in withTestDriver { driver =>
      pending
    }

    "allow see transaction by item creator" in withTestDriver { driver =>
      pending
    }

    "forbid see transaction by non-winner or non-creator" in withTestDriver { driver =>
      pending
    }

  }

}
