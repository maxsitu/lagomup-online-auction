package com.example.auction.item.domain

import com.example.auction.item.impl.ItemApplication
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.ReadSide
import com.lightbend.lagom.scaladsl.testkit.{ReadSideTestDriver, ServiceTest}
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}

class ItemEventProcessorSpec extends AsyncWordSpec with BeforeAndAfterAll with Matchers {

  val server = ServiceTest.startServer(ServiceTest.defaultSetup.withCassandra()) { ctx =>
    new ItemApplication(ctx) {

      override def serviceLocator: ServiceLocator = NoServiceLocator

      override lazy val readSide: ReadSide = new ReadSideTestDriver()

    }
  }

  override def afterAll(): Unit = server.stop()

  val testDriver = server.application.readSide

  "The item event processor" should {
    "create an item" in {
      pending
    }
  }

}
