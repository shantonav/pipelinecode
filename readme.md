## Business logic as pipeline code

### Objective
We are going to see how we can write Java business logic as pipeline code.

- This involves usage of the powerful CompletableFuture of JDK
- Stream derivatives

## Use case

There is an orchestration service let's call is DoSomething , that has an entry point 
which collects responses over 2 services A and B (B depends on the response of A)
and finally produces a composite response which is useful in DoSomethings core logic.


![Use case in UML](UseCase.png)

Class diagram
![Class diagram](ClassDiagram.png)