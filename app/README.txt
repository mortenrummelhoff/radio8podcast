https://developer.android.com/topic/architecture

requirements: functional vs. non-functional (*.ility, e.g portability, testability )

Most important separation of concerns:
1) Example: Navigation component. Layered app structure (presentation -> Models/Services(business logic) -> Data)
Layered architecture:
UI model/state/view
encapsulation


2) Single point of truth (database originated)


3) Data/State flows from top to button. (data exposed in immutable types)

-> {
lambda block
}

compose:

declarative design approach

state -> value that can change over time
recompose over state changes

reusable components


SOLID (
Single Responsibility Principle (High cohesion / low coupling Good:))
Open/Close Principle (open for extension / close for modification)
Liskov Substitution Principle (LSP)
Interface Segregation Principle (the more the better avoiding violation of LSP)
Dependency inversion Principle (depend on abstraction and not concrete classes)
)


Kotlin used in compose:

Highorder-function and lambda's
(trailing lambdas)
Scope and receivers
Delegated properties
Destructuring data classes
Singleton objects (static -> companion)
typesafe builders and DLS
Coroutines