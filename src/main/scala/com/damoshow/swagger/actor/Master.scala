package com.damoshow.swagger.actor

import akka.actor.{Actor, ActorLogging, ActorRef, SupervisorStrategy, Terminated}
//import com.damoshow.swagger.service.UserHttpService

/**
  *
  */
/*
class Master extends Actor with ActorLogging {
  override def supervisorStrategy: SupervisorStrategy = SupervisorStrategy.stoppingStrategy

  private val userRepository = context.watch(createUserRepository())
  context.watch(createHttpService(userRepository))

  log.info("User Master Up and running")

  override def receive = {
    case Terminated(actor) =>
      onTerminated(actor)
  }


  protected def createUserRepository(): ActorRef = {
    context.actorOf(UserRepository.props(), UserRepository.Name)
  }

  protected def createHttpService(userRepositoryActor: ActorRef): ActorRef = {
    context.actorOf(
      UserHttpService.props(address, port, selfTimeout, userRepositoryActor),
      UserHttpService.Name
    )
  }

  protected def onTerminated(ref: ActorRef): Unit = {
    log.error("Terminating the system because {} terminated!", ref)
    context.system.terminate()
  }
}
*/
