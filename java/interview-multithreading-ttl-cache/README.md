# Build It: one load per key

Build a small in-memory cache with a fixed positive time-to-live (TTL). Assume non-null keys and values. A loader starts asynchronous work and promptly returns a completion stage; your cache must never wait for it.

**40 minutes, tests first.** Choose your API and control time in tests. Write a failing JUnit test for each new behavior. From `java/`: `./gradlew :interview-multithreading-ttl-cache:test`. The starter test exercises JUnit and Mockito only.

1. **Store and read (5 min).** Put a value, read it, overwrite it, and distinguish a cached value from a miss.
2. **Expire (8 min).** Use a supplied clock. With a 10-second TTL, a value written at 12:00:00 is visible at 12:00:09 and absent at 12:00:10. Reads do not extend its lifetime.
3. **Share a miss (15 min).** Add `getOrLoad`: a hit returns a completed stage; callers missing the same key concurrently share one loader invocation and its outcome. Reads of a loading key report a miss. Other keys make progress independently. A loaded value's TTL starts when installed, not when requested. An explicit put during a load wins the cache slot; callers already attached to the load still receive its outcome.
4. **Recover (12 min).** If the loader throws before returning a stage, or the stage fails later, waiting callers receive a failure. No failure is cached: the next miss after that failure is delivered can retry. A failed or late load must not erase an intervening put or newer load.

Use no blocking waits or service locks. [Community practice inspiration](https://github.com/tabmadi/revolut-interview-prep); the four-stage contract is original. [Solution notes](SOLUTION.md).
