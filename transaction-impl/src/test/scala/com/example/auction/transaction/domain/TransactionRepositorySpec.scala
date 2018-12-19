package com.example.auction.transaction.domain

import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

import akka.Done
import akka.persistence.query.Sequence
import com.datastax.driver.core.utils.UUIDs
import com.example.auction.transaction.impl._
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.testkit.{ReadSideTestDriver, ServiceTest}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

import scala.concurrent.Future

class TransactionRepositorySpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  val server = ServiceTest.startServer(ServiceTest.defaultSetup.withCassandra()) { ctx =>
    new TransactionApplication(ctx) {
      override def serviceLocator: ServiceLocator = NoServiceLocator

      override lazy val readSide = new ReadSideTestDriver()
    }
  }

  override def afterAll(): Unit = server.stop()

  val testDriver = server.application.readSide
  val transactionRepository = server.application.transactionRepository
  val offset = new AtomicInteger()

  val itemId = UUIDs.timeBased()
  val creatorId = UUID.randomUUID
  val winnerId = UUID.randomUUID
  val itemTitle = "title"
  val currencyId = "EUR"
  val itemPrice = 2000
  val itemData = ItemData(itemTitle, "desc", currencyId, 1, 10, 10, None)
  val transaction = TransactionAggregate(itemId, creatorId, winnerId, itemData, itemPrice, None, None, None)

  val deliveryData = DeliveryData("Addr1", "Addr2", "City", "State", 27, "Country")
  val deliveryPrice = 500
  val payment = Payment("Payment sent via wire transfer")

  "The transaction repository" should {

    "get transaction started for creator" in {
      shouldGetTransactionStarted(creatorId)
    }

    "get transaction started for winner" in {
      pending
    }

    "update status to payment pending for creator" in {
      pending
    }

    "update status to payment pending for winner" in {
      pending
    }

    "update status to payment submitted for creator" in {
      pending
    }

    "update status to payment submitted for winner" in {
      pending
    }

    "update status to payment confirmed after approval for creator" in {
      pending
    }

    "update status to payment confirmed after approval for winner" in {
      pending
    }

    "update status to payment pending after rejection for creator" in {
      pending
    }

    "update status to payment pending after rejection for winner" in {
      pending
    }

    "paginate transaction retrieval" in {
      pending
    }
  }

  // Helpers -----------------------------------------------------------------------------------------------------------

  private def shouldGetTransactionStarted(userId: UUID) = {
    for {
      _ <- feed(TransactionStarted(itemId, transaction))
      transactions <- getTransactions(userId, "NegotiatingDelivery")
    } yield {
      transactions should have size 1
      val expected: TransactionSummary = TransactionSummary(itemId, creatorId, winnerId, itemTitle, currencyId, itemPrice, "NegotiatingDelivery")
      val actual: TransactionSummary = transactions.head
      actual should ===(expected)
    }
  }

  // TODO: Paging
  // TODO: Status enum
  private def getTransactions(userId: UUID, transactionStatus: String): Future[Seq[TransactionSummary]] = {
    transactionRepository.selectUserTransactions(userId, transactionStatus, 10)
  }

  private def feed(event: TransactionEvent): Future[Done] = {
    // TODO: Event needs itemId
    testDriver.feed(event.itemId.toString, event, Sequence(offset.getAndIncrement))
  }

}
