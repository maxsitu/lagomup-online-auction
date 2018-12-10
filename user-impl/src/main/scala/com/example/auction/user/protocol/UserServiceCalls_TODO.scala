package com.example.auction.user.protocol


import com.example.auction.user.api._
import akka.stream.scaladsl.Source
import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.lightbend.lagom.scaladsl.pubsub.{PubSubRegistry, TopicId}
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import scala.concurrent.{ExecutionContext, Future}

trait UserServiceCalls_TODO {
  implicit val ec: ExecutionContext
  implicit val mat: Materializer

  
  val entityRegistry: PersistentEntityRegistry
val db: CassandraSession
val pubSubRegistry: PubSubRegistry
val actorSystem: ActorSystem



  def _createUser(request: CreateUser): Future[User] = {
  ???
}

def _getUser(userId: String, request: NotUsed): Future[User] = {
  ???
}

def _getUsers(request: NotUsed): Future[List[User]] = {
  ???
}



  

}

