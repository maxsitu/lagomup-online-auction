package com.example.auction.transaction.protocol

import com.example.auction.item.api._
import com.example.auction.transaction.api._
import com.example.auction.item.api._

import com.example.auction.transaction.impl.TransactionPorts
import akka.stream.scaladsl.Source
import akka.{ Done, NotUsed }
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import com.lightbend.lagom.scaladsl.pubsub.PubSubRegistry

import scala.concurrent.{ ExecutionContext, Future }

trait ItemEventSubscriber_TODO {

  val entityRegistry: PersistentEntityRegistry

  def onItemEvent(event: ItemEvent): Future[Done] = ???

}

