package com.example.auction.transaction.protocol

import java.time.Instant
import java.util.UUID

import akka.{Done, NotUsed}
import com.datastax.driver.core.utils.UUIDs
import com.example.auction.item.api._
import com.example.auction.transaction.api._
import com.example.auction.transaction.impl.TransactionApplication
import com.example.auction.utils.ClientSecurity
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.{AdditionalConfiguration, ServiceCall}
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.{ProducerStub, ProducerStubFactory, ServiceTest}
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import play.api.Configuration

import scala.concurrent.Await
import scala.concurrent.duration._

class TransactionServiceCallsSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll with Eventually with ScalaFutures {

  var itemEventProducerStub: ProducerStub[ItemEvent] = _

  val server = ServiceTest.startServer(ServiceTest.defaultSetup.withCassandra()) { ctx =>
    new TransactionApplication(ctx) with LocalServiceLocator {

      val stubFactory = new ProducerStubFactory(actorSystem, materializer)
      itemEventProducerStub = stubFactory.producer[ItemEvent]("item-ItemEvent")

      override val itemService: ItemService = new ItemServiceStub(itemEventProducerStub)

      override def additionalConfiguration: AdditionalConfiguration =
        super.additionalConfiguration ++ Configuration.from(Map(
          "cassandra-query-journal.eventual-consistency-delay" -> "0",
          "lagom.circuit-breaker.default.enabled" -> "off"
        ))

    }
  }

  override def afterAll(): Unit = server.stop()

  val transactionService = server.serviceClient.implement[TransactionService]

  val awaitTimeout = 10.seconds

  def fixture = new {
    val itemId = UUIDs.timeBased()
    val creatorId = UUID.randomUUID()
    val winnerId = UUID.randomUUID()
    val itemPrice = 5000
    val itemData = ItemData("title", "desc", "EUR", 1, 10, 10, None)
    val item = Item(Some(itemId), creatorId, itemData, Some(itemPrice), "Completed", Some(Instant.now), Some(Instant.now), Some(winnerId))
    val auctionFinished = AuctionFinished(itemId, item)

    val deliveryInfo = DeliveryInfo("ADDR1", "ADDR2", "CITY", "STATE", 27, "COUNTRY")
    val deliveryPrice = 500
    val paymentInfo = PaymentInfo("Payment sent via wire transfer")

    val transactionInfoStarted = TransactionInfo(itemId, creatorId, winnerId, itemData, itemPrice, None, None, None, "NegotiatingDelivery")
    val transactionInfoWithDeliveryInfo = TransactionInfo(itemId, creatorId, winnerId, itemData, item.price.get, Some(deliveryInfo), None, None, "NegotiatingDelivery")
    val transactionInfoWithDeliveryPrice = TransactionInfo(itemId, creatorId, winnerId, itemData, item.price.get, None, Some(deliveryPrice), None, "NegotiatingDelivery")
    val transactionInfoWithPaymentPending = TransactionInfo(itemId, creatorId, winnerId, itemData, item.price.get, Some(deliveryInfo), Some(deliveryPrice), None, "PaymentPending")
    val transactionInfoWithPaymentDetails = TransactionInfo(itemId, creatorId, winnerId, itemData, item.price.get, Some(deliveryInfo), Some(deliveryPrice), Some(paymentInfo), "PaymentSubmitted")
    val transactionInfoWithPaymentApproved = TransactionInfo(itemId, creatorId, winnerId, itemData, item.price.get, Some(deliveryInfo), Some(deliveryPrice), Some(paymentInfo), "PaymentConfirmed")
    val transactionInfoWithPaymentRejected = TransactionInfo(itemId, creatorId, winnerId, itemData, item.price.get, Some(deliveryInfo), Some(deliveryPrice), Some(paymentInfo), "PaymentPending")
  }

  "The transaction service" should {

    "create transaction on auction finished" in {
      //      val f = fixture
      //      itemEventProducerStub.send(f.auctionFinished)
      //      eventually(timeout(Span(10, Seconds))) {
      //        retrieveTransaction(f.itemId, f.creatorId) should ===(f.transactionInfoStarted)
      //      }
      pending
    }

    "not create transaction with no winner" in {
      pending
    }

    "submit delivery details" in {
      pending
    }

    "set delivery price" in {
      pending
    }

    "approve delivery details" in {
      pending
    }

    "forbid approve empty delivery details" in {
      pending
    }

    "submit payment details" in {
      pending
    }

    "approve payment" in {
      pending
    }

    "reject payment" in {
      pending
    }

  }

  // Helpers -----------------------------------------------------------------------------------------------------------

  private def retrieveTransaction(itemId: UUID, creatorId: UUID): TransactionInfo = {
    Await.result(
      transactionService.getTransaction(itemId)
        .handleRequestHeader(ClientSecurity.authenticate(creatorId))
        .invoke(),
      awaitTimeout
    )
  }

}

class ItemServiceStub(itemEventProducerStub: ProducerStub[ItemEvent]) extends ItemService {

  def createItem(): ServiceCall[Item, Item] = ???

  def startAuction(id: UUID): ServiceCall[NotUsed, Done] = ???

  def getItem(id: UUID): ServiceCall[NotUsed, Item] = ???

  def getItemsForUser(id: UUID, status: String, page: Option[String]): ServiceCall[NotUsed, ItemSummaryPagingState] = ???

  def itemEvents: Topic[ItemEvent] = itemEventProducerStub.topic

}
