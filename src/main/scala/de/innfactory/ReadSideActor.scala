package de.innfactory
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors, Routers}
import akka.util.Timeout
import de.innfactory.Account.{ActorState, AdjustBalance, BalanceAdjusted, DepositMoney, GetBalance, MoneyDeposited}
import de.innfactory.AccountSupervisor.Accounts
import de.innfactory.ReadSideActor.{ActorStatus, AggregatedDeposit, GetAggregatedDeposit}
import de.innfactory.common.{Cmd, Entity}

import scala.concurrent.duration.{FiniteDuration, SECONDS}
object ReadSideActor {

  case class ActorStatus(actorId: String, balance: Int, currentSequenceNr: Int)
  case class AggregatedDeposit(amount: Int) extends Cmd
  case class GetAggregatedDeposit(returnTo: ActorRef[Cmd]) extends Cmd

  def apply(): Behavior[Entity] =
    Behaviors.setup(context => new ReadSideActor(context))
}

class ReadSideActor(context: ActorContext[Entity]) extends AbstractBehavior[Entity](context) {

  private var accounts: Map[String, ActorRef[Cmd]] = Map.empty
  private var dataView: Map[String, ActorStatus] = Map.empty
  private var aggregatedDeposit: Int = 0

  override def onMessage(message: Entity): Behavior[Entity] = {
    message match {
      case Accounts(ac) => {
        accounts = ac
        for (account <- accounts) {
          dataView = dataView + (account._1 -> ActorStatus(account._1, 0, 0))
        }
      }
      case BalanceAdjusted(adjusted, sequenceNr, actorId) => {
        dataView = dataView.map {
          case (key, value) => if (key.equals(actorId)) {
            (key -> ActorStatus(actorId, value.balance + adjusted, sequenceNr))
          } else {
            (key -> value)
          }
        }
      }
      case MoneyDeposited(amount, sequenceNr, actorId) => {
        aggregatedDeposit += amount
        dataView = dataView.map{
          case (key, value) => if (key.equals(actorId)) {
            (key -> ActorStatus(actorId, value.balance + amount, sequenceNr))
          }else {
            (key -> value)
          }
        }
      }
      case GetBalance(returnTo, actorId) => {
        context.log.info(context.self.path + " GetBalance")
        val value = dataView.get(actorId)
        value match {
          case Some(ActorStatus(id, balance, currentSequenceNr)) => returnTo ! ActorState(id, balance)
          case None => context.log.info("NONE")
        }
      }
      case GetAggregatedDeposit(returnTo) => {
        returnTo ! AggregatedDeposit(aggregatedDeposit)
      }

    }
    this
  }
}
