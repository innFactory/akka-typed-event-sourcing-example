package de.innfactory

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import de.innfactory.Account.{ActorState, AdjustBalance, DepositMoney, GetBalance}
import de.innfactory.AccountSupervisor.Accounts
import de.innfactory.common.Cmd

object CommandBot {

  def apply(): Behavior[Cmd] =
    Behaviors.setup(context => new CommandBot(context))
}

class CommandBot(context: ActorContext[Cmd]) extends AbstractBehavior[Cmd](context) {

  private var accounts: Map[Int, ActorRef[Cmd]] = Map.empty

  def botProcessing(): Unit = {
    val r = new scala.util.Random
    for(account <- accounts) {
      for(random <- 0 to r.between(2, 8)) {
        if(r.between(0,2).equals(0)) {
          account._2 ! DepositMoney(r.between(10, 200))
        } else {
          account._2 ! AdjustBalance(r.between(-100,100) + random)
        }

      }
      account._2 ! GetBalance(context.self)
    }
  }

  override def onMessage(message: Cmd): Behavior[Cmd] = {
    message match {
      case Accounts(messageAccounts) => {
        accounts = messageAccounts
        botProcessing()
      }
      case ActorState(id, balance) =>
    }
    this
  }
}
