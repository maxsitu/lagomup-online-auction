package com.example.auction.user.domain

import com.example.auction.user.impl._
import akka.Done
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity

trait UserDomain_TODO {
  this: UserEntity with PersistentEntity =>

  //def userEventStream: UserEventStream

  def initialState: UserState = ???

  def onCreateUser(command: CreateUser, state: UserState, ctx: CommandContext[Done]): Persist = {
    ???
  }

  def onGetUser(query: GetUser.type, state: UserState, ctx: ReadOnlyCommandContext[Option[UserAggregate]]): Unit = {
    ???
  }

  def onUserCreated(event: UserCreated, state: UserState): UserState = {
    ???
  }

}

