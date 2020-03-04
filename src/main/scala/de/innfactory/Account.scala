package de.innfactory
import akka.actor.typed.receptionist.Receptionist
import de.innfactory.common.{Cmd, Entity, Event}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import de.innfactory.Account.{ActorState, AdjustBalance, BalanceAdjusted, DepositMoney, GetBalance, MoneyDeposited}

object Account {
  final case class AdjustBalance(adjust: Int) extends Cmd
  final case class DepositMoney(amount: Int) extends Cmd
  final case class GetBalance(returnTo: ActorRef[Cmd], actorId: String) extends Cmd
  final case class BalanceAdjusted(adjusted: Int, sequenceNr: Int, actorId: String) extends Event
  final case class MoneyDeposited(amount: Int, sequenceNr: Int, actorId: String) extends Event
  final case class ActorState(id: String, balance: Int) extends Cmd

  def apply(id: String, seq: Seq[ActorRef[Entity]]): Behavior[Cmd] =
    Behaviors.setup(context => new Account(context, id, seq))
}

class Account(context: ActorContext[Cmd], id: String, seq: Seq[ActorRef[Entity]]) extends AbstractBehavior[Cmd](context) {
  var actorId: String = id
  private var accountState = ActorState(actorId, 0)
  private var events = Seq.empty[Event]

  private def receiveCmd(cmd: Cmd): Unit = {
    cmd match {
      case DepositMoney(amount) => alterState(cmd)
      case AdjustBalance(adjust) => alterState(cmd)

      case GetBalance(returnTo, actorId) => {
      context.log.info(actorId + " Replaying from Events " + events.toString() + ", state would be " + replay().balance + " current state was " + accountState.balance)
      returnTo ! accountState
    }
  }}

  private def replay() = {
    var value = events.map {
      case BalanceAdjusted(adjusted, sequenceNr, id) => adjusted
      case MoneyDeposited(amount, sequenceNr,id) => amount
      case _ => 0
    }.sum

    ActorState(actorId, value)
  }

  private def getCurrentSequenceNr:Int = {
    events.length
  }

  private def processEvent(evt: Event) = {
    for(actorRef <- seq) {
      actorRef ! evt
    }
    events = events.appended(evt)
  }

  private def alterState(cmd: Cmd): Unit = cmd match {
    case DepositMoney(amount) => {
     processEvent(MoneyDeposited(amount,getCurrentSequenceNr + 1, actorId))
      accountState = ActorState(actorId, accountState.balance + amount)
    }
    case AdjustBalance(adjust) => {
      processEvent(BalanceAdjusted(adjust, getCurrentSequenceNr + 1, actorId))
      accountState = ActorState(actorId, accountState.balance + adjust)
    }
  }

  override def onMessage(message: Cmd): Behavior[Cmd] = {
    receiveCmd(message)
    this
  }
}
