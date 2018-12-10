package com.example.auction.user.protocol

import java.util.UUID

import akka.NotUsed
import com.example.auction.user.api._
import com.example.auction.user.impl
import com.example.auction.user.impl.{GetUser, UserEntity}
import com.lightbend.lagom.scaladsl.api.transport.NotFound
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import com.lightbend.lagom.scaladsl.pubsub.PubSubRegistry

import scala.concurrent.{ExecutionContext, Future}

trait UserServiceCalls {
  implicit val ec: ExecutionContext

  val entityRegistry: PersistentEntityRegistry
  val db: CassandraSession
  val pubSubRegistry: PubSubRegistry

  def _createUser(request: CreateUser): Future[User] = {
    val userId = UUID.randomUUID().toString // TODO: UUID
    entityRegistry.refFor[UserEntity](userId).ask(impl.CreateUser(request.name)).map { _ =>
      User(userId, request.name)
    }
  }

  def _getUser(userId: String, request: NotUsed): Future[User] = {
    entityRegistry.refFor[UserEntity](userId).ask(GetUser).map {
      case Some(userState) => User(userId, userState.name)
      case None => throw NotFound(s"User with id $userId")
    }
  }

  def _getUsers(request: NotUsed): Future[List[User]] = {
    ???
  }

}

