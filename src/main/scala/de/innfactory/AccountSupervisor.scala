package de.innfactory

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import de.innfactory.AccountSupervisor.Start
import de.innfactory.common.{Cmd, Entity}

object AccountSupervisor {
  final case class Start(returnAccountsTo: ActorRef[Cmd]) extends Cmd
  final case class Accounts(accounts: Map[String, ActorRef[Cmd]]) extends Cmd

  def apply(seq: Seq[ActorRef[Entity]]): Behavior[Start] =
    Behaviors.setup(context => new AccountSupervisor(context, seq))
}

class AccountSupervisor(context: ActorContext[Start],seq: Seq[ActorRef[Entity]]) extends AbstractBehavior[Start](context) {
  import AccountSupervisor._
  var accounts: Map[String, ActorRef[Cmd]] = Map.empty[String, ActorRef[Cmd]]

  override def onMessage(message: Start): Behavior[Start] = {
    if(accounts.isEmpty) {
      for(i <- 1 to 10) {
        accounts += ("AccountActor"+i -> context.spawn(Account("AccountActor"+i, seq), "AccountActor"+i) )
      }
      for(actor <- seq) {
        actor ! Accounts(accounts)
      }
      message.returnAccountsTo ! Accounts(accounts)
    }
    this
  }
}
