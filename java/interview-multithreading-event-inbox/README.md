# Build It: who owns this event?

An in-memory inbox receives events with stable IDs. Several workers pull from the same inbox. Finishing out of arrival order is fine; event loss and duplicate active claims are not.

**40 minutes, tests first.** Choose your API. Write a failing JUnit test for the next observable behavior before implementing it. From `java/`: `./gradlew :interview-multithreading-event-inbox:test`. The starter test exercises JUnit and Mockito only.

1. **Submit (5 min).** Submit an ID and payload. Report whether an ID is ready for work. An unknown ID is not ready.
2. **Deduplicate (8 min).** Race two submissions of the same ID. Exactly one succeeds, and its payload is retained. Later submissions of that ID always report duplicate, even after processing.
3. **Claim (12 min).** Claim any ready event, returning its ID, payload, and a claim token, or report empty immediately. With one event and two workers, only one gets a claim. Acknowledge the current token to mark the event done; it cannot be claimed again.
4. **Redeliver (15 min).** Releasing the current claim makes that event ready again. Each new claim carries a fresh token. After A releases and B claims, A's late acknowledgement or release must fail. If acknowledgement and release race for the *same* current token, exactly one succeeds.

Original practice variant. Keep operations non-blocking: no waiting on a worker, a lock, or a future event. [Solution notes](SOLUTION.md).
