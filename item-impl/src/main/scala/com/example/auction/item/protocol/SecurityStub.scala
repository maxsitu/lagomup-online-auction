package com.example.auction.item.protocol

import java.security.Principal
import java.util.UUID

import com.lightbend.lagom.scaladsl.api.security.ServicePrincipal
import com.lightbend.lagom.scaladsl.api.transport.{Forbidden, RequestHeader}
import com.lightbend.lagom.scaladsl.server.ServerServiceCall
import javax.security.auth.Subject

sealed trait UserPrincipal extends Principal {
  val userId: UUID
  override def getName: String = userId.toString
  override def implies(subject: Subject): Boolean = false
}

object UserPrincipal {
  case class ServicelessUserPrincipal(userId: UUID) extends UserPrincipal
  case class UserServicePrincipal(userId: UUID, servicePrincipal: ServicePrincipal) extends UserPrincipal with ServicePrincipal {
    override def serviceName: String = servicePrincipal.serviceName
  }

  def of(userId: UUID, principal: Option[Principal]) = {
    principal match {
      case Some(servicePrincipal: ServicePrincipal) =>
        UserPrincipal.UserServicePrincipal(userId, servicePrincipal)
      case other =>
        UserPrincipal.ServicelessUserPrincipal(userId)
    }
  }
}

object ServerSecurity {

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

object ClientSecurity {

  /**
    * Authenticate a client request.
    */
  def authenticate(userId: UUID): RequestHeader => RequestHeader = { request =>
    request.withPrincipal(UserPrincipal.of(userId, request.principal))
  }

}
