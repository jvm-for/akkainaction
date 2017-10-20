package com.damoshow.swagger.actor

import akka.actor.{Actor, ActorLogging, Props}

/**
  *
  */
object UserRepository {
  case class User(name: String)
  case class AddUser(name: String)
  case object GetUsers
  case class UserAdded(user: User)
  case class UserExists(name: String)

  final val Name = "user-repository"

  def props(): Props = Props(new UserRepository())
}

class UserRepository extends Actor with ActorLogging {
  import UserRepository._

  private var users = Set.empty[User]

  override def receive: Receive = {
    case GetUsers =>
      log.info("received GetUsers command")
      sender() ! users

    case AddUser(name) if users.exists(_.name == name) =>
      sender() ! UserExists(name)

    case AddUser(name) =>
      log.info(s"Adding new user with name: $name")
      val user = User(name)
      users = users + user
      sender() ! UserAdded(user)
  }
}