package com.example.auction.bidding.protocol

import java.security.Principal

import com.lightbend.lagom.scaladsl.api.transport.Forbidden
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import javax.security.auth.Subject

sealed trait UserPrincipal extends Principal {
  val userId: String // TODO: UUID
  override def getName: String = userId

  override def implies(subject: Subject): Boolean = false
}

object ServerSecurity {

  // TODO: UUID
  def authenticated[Request, Response](serviceCall: String => ServerServiceCall[Request, Response]) =
    ServerServiceCall.compose { requestHeader =>
      requestHeader.principal match {
        case Some(userPrincipal: UserPrincipal) =>
          serviceCall(userPrincipal.userId)
        case other =>
          throw Forbidden("User not authenticated")
      }
    }

}
