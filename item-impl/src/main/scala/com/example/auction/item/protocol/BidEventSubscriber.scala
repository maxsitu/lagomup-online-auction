package com.example.auction.item.protocol

import com.example.auction.bidding.api._
import com.example.auction.item.api._
import com.example.auction.bidding.api._

import com.example.auction.item.impl.ItemPorts
import akka.stream.scaladsl.Source
import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import com.lightbend.lagom.scaladsl.pubsub.PubSubRegistry

import scala.concurrent.{ExecutionContext, Future}

trait BidEventSubscriber {

  val entityRegistry: PersistentEntityRegistry

  def onBidEvent(event: BidEvent): Future[Done] = ???

}

