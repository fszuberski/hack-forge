# Build It: a consistent FX quote book

Dealers publish exchange rates identified by pair and sequence number. Clients read quotes and compare pairs. Everything lives in memory. Use positive integer price ticks so floating-point rounding is outside the exercise.

**40 minutes, tests first.** Choose your API. Write a failing JUnit test before each change. From `java/`: `./gradlew :interview-multithreading-fx-quotes:test`. The starter test exercises JUnit and Mockito only.

1. **Publish a price (5 min).** Publish and retrieve the latest price for one pair. An unknown pair is absent. Updating EUR/USD must not change GBP/USD.
2. **Order the feed (10 min).** Each update now has a positive sequence number. Accept only a strictly newer sequence *for that pair*. Race sequences 42 and 43 against 41: 43 must succeed and be the final quote; 42's result depends on which arrived first.
3. **Quote a spread (10 min).** Each quote now carries bid and ask, with `bid <= ask`. Reject invalid quotes without advancing the sequence. A reader racing two updates observes an entire published quote: never a bid from one update with an ask from another.
4. **Publish a pair of quotes (15 min).** Provide one atomic batch for two distinct pairs and one snapshot read. Publish both only if both updates are valid and newer than their respective current quotes; otherwise change neither. Racing a successful batch with a snapshot read yields the whole old book or the whole new book, never a mixture.

Original practice variant. No reader or writer may wait for another caller to release a lock. [Solution notes](SOLUTION.md).
