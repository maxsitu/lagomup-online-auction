package com.example.auction.transaction.api

import com.example.auction.item.api._

import com.example.auction.utils.SecurityHeaderFilter
import akka.{ Done, NotUsed }
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{ KafkaProperties, PartitionKeyStrategy }
import akka.util.ByteString
import com.lightbend.lagom.scaladsl.api.deser.MessageSerializer
import com.lightbend.lagom.scaladsl.api.{ Service, ServiceCall }
import com.lightbend.lagom.scaladsl.playjson.JsonSerializer
import com.lightbend.lagom.scaladsl.api.transport.Method
import julienrf.json.derived
import play.api.libs.json._
import java.time.Instant
import java.util.UUID

trait TransactionService extends Service {
  def submitDeliveryDetails(itemId: UUID): ServiceCall[DeliveryInfo, Done]

  def setDeliveryPrice(itemId: UUID): ServiceCall[Int, Done]

  def approveDeliveryDetails(itemId: UUID): ServiceCall[NotUsed, Done]

  def submitPaymentDetails(itemId: UUID): ServiceCall[PaymentInfo, Done]

  def submitPaymentStatus(itemId: UUID): ServiceCall[String, Done]

  def getTransaction(itemId: UUID): ServiceCall[NotUsed, TransactionInfo]

  def getTransactionsForUser(status: String, pageNo: Option[String], pageSize: Option[String]): ServiceCall[NotUsed, List[TransactionSummary]]

  override def descriptor = {
    import Service._

    named("transaction")
      .withCalls(
        pathCall("/api/transaction/:id/deliverydetails", submitDeliveryDetails _)(implicitly[MessageSerializer[DeliveryInfo, ByteString]], implicitly[MessageSerializer[Done, ByteString]]),
        pathCall("/api/transaction/:id/deliveryprice", setDeliveryPrice _)(implicitly[MessageSerializer[Int, ByteString]], implicitly[MessageSerializer[Done, ByteString]]),
        pathCall("/api/transaction/:id/approvedelivery", approveDeliveryDetails _)(implicitly[MessageSerializer[NotUsed, ByteString]], implicitly[MessageSerializer[Done, ByteString]]),
        pathCall("/api/transaction/:id/paymentdetails", submitPaymentDetails _)(implicitly[MessageSerializer[PaymentInfo, ByteString]], implicitly[MessageSerializer[Done, ByteString]]),
        pathCall("/api/transaction/:id/paymentstatus", submitPaymentStatus _)(implicitly[MessageSerializer[String, ByteString]], implicitly[MessageSerializer[Done, ByteString]]),
        pathCall("/api/transaction/:id", getTransaction _)(implicitly[MessageSerializer[NotUsed, ByteString]], implicitly[MessageSerializer[TransactionInfo, ByteString]]),
        pathCall("/api/transaction?status&pageNo&pageSize", getTransactionsForUser _)(implicitly[MessageSerializer[NotUsed, ByteString]], implicitly[MessageSerializer[List[TransactionSummary], ByteString]])
      )

      .withHeaderFilter(SecurityHeaderFilter.Composed)
      .withAutoAcl(true)
  }

}

case class DeliveryInfo(addressLine1: String, addressLine2: String, city: String, state: String, postalCode: Int, country: String)

object DeliveryInfo {
  implicit val format: Format[DeliveryInfo] = Json.format
}

case class PaymentInfo(comment: String)

object PaymentInfo {
  implicit val format: Format[PaymentInfo] = Json.format
}

case class TransactionInfo(itemId: UUID, creator: UUID, winner: UUID, itemData: ItemData, itemPrice: Int, deliveryInfo: Option[DeliveryInfo], deliveryPrice: Option[Int], paymentInfo: Option[PaymentInfo], status: String)

object TransactionInfo {
  implicit val format: Format[TransactionInfo] = Json.format
}

case class TransactionSummary(itemId: UUID, creator: UUID, winnerId: UUID, itemTitle: String, currencyId: String, itemPrice: Int, status: String)

object TransactionSummary {
  implicit val format: Format[TransactionSummary] = Json.format
}

