# Akka Event Sourcing Example

This example [akka](https://github.com/akka/akka) project should represent the concept of event sourcing.  
It contains four Types of Actors

- RootActor: Holds the "AccountSupervisor" and "CommandBot" actors
- AccountSupervisor: Starts the "Account" actors and supervises them
- CommandBot: Sends a random amount of Commands to the Accounts
- Account: Holds a list of received events and a state which is derived from those

## Account Entities:

### UML:

![Account][accountuml] 

[accountuml]: ./assets/account-uml.png "Account" 

### Commands:

#### AdjustBalance 

Command which gets converted to [BalanceAdjusted](#balanceadjusted) after processing.  
Tells the Account to adjust its balance based on the "adjust" argument.

    final case class AdjustBalance(adjust: Int) extends Cmd 
 
#### DepositMoney

Command which gets converted to [MoneyDeposited](#moneydeposited) after processing.  
Tells the Account to add "amount" to its balance.
    
    final case class DepositMoney(amount: Int) extends Cmd
    
#### GetBalance 

Account logs its current balance and is trying to replay all events to reproduce the current ActorState.
    
    final case class GetBalance(returnTo: ActorRef[Cmd]) extends Cmd

#### ActorState

Represents the current balance of the actor. 
    
    final case class ActorState(id: String, balance: Int) extends Cmd
 
### Events:

#### BalanceAdjusted

Gets saved in the "Account" event Sequence after processing.

    final case class BalanceAdjusted(adjusted: Int, sequenceNr: Int) extends Event
    
#### MoneyDeposited

Gets saved in the "Account" event Sequence after processing.

    final case class MoneyDeposited(amount: Int, sequenceNr: Int) extends Event

## Application Architecture Diagram

![Architecture][architecture]  

[architecture]: ./assets/architecture.png "Architecture"


## Running

To run this application:

```sbt run```

## Testing

To test this application:      

```sbt test```


## Contributors

[Patrick Stadler](https://github.com/patsta32)