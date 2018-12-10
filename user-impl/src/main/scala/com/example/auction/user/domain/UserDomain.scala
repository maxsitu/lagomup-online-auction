package com.example.auction.user.domain

import akka.Done
import com.example.auction.user.impl._
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity
import com.lightbend.lagom.scaladsl.pubsub.PubSubRegistry

trait UserDomain {
  this: UserEntity with PersistentEntity =>

  def pubSubRegistry: PubSubRegistry

  def initialState: UserState = UserState(None, UserAggregateStatus.NotCreated)

  def onCreateUser(command: CreateUser, state: UserState, ctx: CommandContext[Done]): Persist = {
    state.status match {
      case UserAggregateStatus.NotCreated =>
        ctx.thenPersist(UserCreated(command.name))(_ => ctx.reply(Done))
      case UserAggregateStatus.Created =>
        ctx.invalidCommand("User already exists")
        ctx.done
    }
  }

  def onGetUser(query: GetUser.type, state: UserState, ctx: ReadOnlyCommandContext[Option[UserAggregate]]): Unit = {
    ctx.reply(state.aggregate)
  }

  def onUserCreated(event: UserCreated, state: UserState): UserState = {
    UserState(Some(UserAggregate(event.name)), UserAggregateStatus.Created)
  }

}

