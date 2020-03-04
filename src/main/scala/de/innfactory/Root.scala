//#full-example
package de.innfactory


import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors, Routers}
import de.innfactory.common.Cmd

object RootActor {
    def apply(): Behavior[Any] =
    Behaviors.setup(context => new RootActor(context))
}

class RootActor(context: ActorContext[Any]) extends AbstractBehavior[Any](context) {

  private val readSide1 = context.spawn(ReadSideActor(), "readside1")
  private val readSide2 = context.spawn(ReadSideActor(), "readside2")

  private val supervisorActor = context.spawn(AccountSupervisor(Seq(readSide1, readSide2)), "AccountSupervisor")

  private val commandBot = context.spawn(CommandBot(Seq(readSide1, readSide2)), "CommandBot")

  supervisorActor ! AccountSupervisor.Start(commandBot)


  override def onMessage(message: Any): Behavior[Any] = {
   this
  }
}

object AkkaRoot extends App {
  val root: ActorSystem[Any] = ActorSystem(RootActor(), "RootActor")

}

