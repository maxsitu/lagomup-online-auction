package com.example.auction.user.impl

import com.example.auction.utils.JsonFormats._
import com.example.auction.user.domain.UserDomain
import akka.{ Done, NotUsed }
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.persistence.{ AggregateEvent, AggregateEventTag, PersistentEntity }
import com.lightbend.lagom.scaladsl.playjson.{ JsonSerializer, JsonSerializerRegistry }
import com.lightbend.lagom.scaladsl.pubsub.{ PubSubRegistry, TopicId }
import play.api.libs.json.{ Format, Json }
import java.time.Instant
import akka.stream.scaladsl.Source
import java.util.UUID

class UserEntity(val userEventStream: UserEventStream) extends PersistentEntity
  with UserDomain {

  override type State = UserState
  override type Command = UserCommand
  override type Event = UserEvent

  override def behavior: Behavior = {
    Actions()
      .onReadOnlyCommand[StateSnapshot.type, UserState] {
        case (_: StateSnapshot.type, ctx, state) =>
          ctx.reply(state)
      }

      .onCommand[CreateUser, Done] {
        case (command: CreateUser, ctx, state) =>
          onCreateUser(command, state, ctx)
      }

      .onReadOnlyCommand[GetUser.type, Option[UserAggregate]] {
        case (query: GetUser.type, ctx, state) =>
          onGetUser(query, state, ctx)
      }

      .onEvent {
        case (event: UserCreated, state) =>
          onUserCreated(event, state)
      }

  }

}

case class UserAggregate(name: String)

object UserAggregate {
  implicit val format: Format[UserAggregate] = Json.format
}

object UserAggregateStatus extends Enumeration {
  val NotCreated, Created = Value
  type Status = Value
  implicit val format: Format[Status] = enumFormat(UserAggregateStatus)
}

case class UserState(aggregate: Option[UserAggregate], status: UserAggregateStatus.Status)

object UserState {
  implicit val format: Format[UserState] = Json.format
}

sealed trait UserCommand

case object StateSnapshot extends UserCommand with ReplyType[UserState] {
  implicit val format: Format[StateSnapshot.type] = JsonSerializer.emptySingletonFormat(StateSnapshot)
}

case class CreateUser(name: String) extends UserCommand with ReplyType[Done]

object CreateUser {
  implicit val format: Format[CreateUser] = Json.format
}

case object GetUser extends UserCommand with ReplyType[Option[UserAggregate]] {
  implicit val format: Format[GetUser.type] = JsonSerializer.emptySingletonFormat(GetUser)
}

sealed trait UserEvent extends AggregateEvent[UserEvent] {
  val name: String

  def aggregateTag = UserEvent.Tag
}

object UserEvent {
  val Tag = AggregateEventTag[UserEvent]
}

case class UserCreated(name: String) extends UserEvent

object UserCreated {
  implicit val format: Format[UserCreated] = Json.format
}

trait UserEventStream {

  def publish(qualifier: String, event: UserEvent): Unit

  def subscriber(qualifier: String): Source[UserEvent, NotUsed]

}

class UserEventStreamImpl(pubSubRegistry: PubSubRegistry) extends UserEventStream {

  def publish(qualifier: String, event: UserEvent): Unit = {
    pubSubRegistry.refFor(TopicId[UserEvent](qualifier)).publish(event)
  }

  def subscriber(qualifier: String): Source[UserEvent, NotUsed] = {
    pubSubRegistry.refFor(TopicId[UserEvent](qualifier)).subscriber
  }

}

object UserSerializerRegistry extends JsonSerializerRegistry {
  override def serializers = List(
    JsonSerializer[UserState],
    JsonSerializer[StateSnapshot.type],
    JsonSerializer[UserAggregate],
    JsonSerializer[CreateUser],
    JsonSerializer[GetUser.type],
    JsonSerializer[UserCreated]
  )
}

