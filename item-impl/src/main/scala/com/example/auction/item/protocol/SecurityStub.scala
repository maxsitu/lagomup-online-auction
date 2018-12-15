package com.example.auction.item.protocol

import java.security.Principal
import java.util.UUID

import com.lightbend.lagom.scaladsl.api.transport.Forbidden
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import javax.security.auth.Subject

sealed trait UserPrincipal extends Principal {
  val userId: UUID
  override def getName: String = userId.toString
  override def implies(subject: Subject): Boolean = false
}

object ServerSecurity {

  // TODO: UUID
  def authenticated[Request, Response](serviceCall: UUID => ServerServiceCall[Request, Response]) =
    ServerServiceCall.compose { requestHeader =>
      requestHeader.principal match {
        case Some(userPrincipal: UserPrincipal) =>
          serviceCall(userPrincipal.userId)
        case other =>
          throw Forbidden("User not authenticated")
      }
    }

}
