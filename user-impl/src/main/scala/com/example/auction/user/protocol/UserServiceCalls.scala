package com.example.auction.user.protocol

import java.util.UUID

import akka.NotUsed
import com.example.auction.user.api._

import scala.concurrent.{ExecutionContext, Future}

trait UserServiceCalls {
  implicit val ec: ExecutionContext

  def _createUser(request: CreateUser): Future[User] = {
    val userId = UUID.randomUUID().toString // TODO: UUID
    ???
  }

  def _getUser(request: NotUsed): Future[User] = {
    ???
  }

  def _getUsers(request: NotUsed): Future[List[User]] = {
    ???
  }

}

