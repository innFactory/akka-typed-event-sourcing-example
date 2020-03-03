//#full-example
package de.innfactory

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import de.innfactory.Account.{ActorState, AdjustBalance, GetBalance}
import de.innfactory.common.Cmd
import org.scalatest.WordSpecLike

class AkkaAccountSpec extends ScalaTestWithActorTestKit with WordSpecLike {
  "A Account" must {
    "reply with correct ActorState" in {
      val replyProbe = createTestProbe[Cmd]()
      val underTest = spawn(Account("1"))
      underTest ! AdjustBalance(10)
      underTest ! GetBalance(replyProbe.ref)
      replyProbe.expectMessage(ActorState("1", 10))
    }
  }
}
