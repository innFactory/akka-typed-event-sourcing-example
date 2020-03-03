package de.innfactory

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import de.innfactory.AccountSupervisor.Start
import de.innfactory.common.Cmd

object AccountSupervisor {
  final case class Start(returnAccountsTo: ActorRef[Cmd]) extends Cmd
  final case class Accounts(accounts: Map[Int, ActorRef[Cmd]]) extends Cmd

  def apply(): Behavior[Start] =
    Behaviors.setup(context => new AccountSupervisor(context))
}

class AccountSupervisor(context: ActorContext[Start]) extends AbstractBehavior[Start](context) {
  import AccountSupervisor._
  var accounts: Map[Int, ActorRef[Cmd]] = Map.empty[Int, ActorRef[Cmd]]

  override def onMessage(message: Start): Behavior[Start] = {
    if(accounts.isEmpty) {
      for(i <- 1 to 10) {
        accounts += (i -> context.spawn(Account("AccountActor"+i), "AccountActor"+i) )
      }
      message.returnAccountsTo ! Accounts(accounts)
    }
    this
  }
}
