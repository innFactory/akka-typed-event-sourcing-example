//#full-example
package de.innfactory


import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import de.innfactory.common.Cmd

object RootActor {
    def apply(): Behavior[Any] =
    Behaviors.setup(context => new RootActor(context))
}

class RootActor(context: ActorContext[Any]) extends AbstractBehavior[Any](context) {

  private val supervisorActor = context.spawn(AccountSupervisor(), "AccountSupervisor")
  private val commandBot = context.spawn(CommandBot(), "CommandBot")

  supervisorActor ! AccountSupervisor.Start(commandBot)

  override def onMessage(message: Any): Behavior[Any] = {
   this
  }
}

object AkkaRoot extends App {
  val root: ActorSystem[Any] = ActorSystem(RootActor(), "RootActor")

}

