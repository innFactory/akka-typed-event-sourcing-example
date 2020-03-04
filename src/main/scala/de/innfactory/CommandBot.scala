package de.innfactory

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.util.Timeout
import de.innfactory.Account.{ActorState, AdjustBalance, DepositMoney, GetBalance}
import de.innfactory.AccountSupervisor.Accounts
import de.innfactory.common.{Cmd, Entity}

import scala.concurrent.duration.SECONDS
import scala.concurrent.duration.FiniteDuration
import scala.util.Success

object CommandBot {

  def apply(seq: Seq[ActorRef[Entity]]): Behavior[Cmd] =
    Behaviors.setup(context => new CommandBot(context, seq))
}

class CommandBot(context: ActorContext[Cmd], seq: Seq[ActorRef[Entity]]) extends AbstractBehavior[Cmd](context) {

  private var accounts: Map[String, ActorRef[Cmd]] = Map.empty
  implicit val timeout: Timeout = FiniteDuration(3, SECONDS)


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
      if(r.nextBoolean()) {
        seq.head ! GetBalance(context.self, account._1)
      } else {
        seq.tail.head ! GetBalance(context.self, account._1)
      }
    }
  }

  override def onMessage(message: Cmd): Behavior[Cmd] = {
    message match {
      case Accounts(messageAccounts) => {
        accounts = messageAccounts
        botProcessing()
      }
      case ActorState(id, balance) => {
        context.log.info("Received Balance for " + id + " balance = " + balance)
      }
    }
    this
  }
}
