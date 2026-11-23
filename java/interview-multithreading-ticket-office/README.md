# Build It: the last ticket

A venue sells general-admission tickets for one performance. Buyers share one in-memory ticket office. There are no seats to assign, payments to process, or restocks. Your job is to tell each buyer whether their **whole** purchase succeeded.

**40-minute TDD drill.** Take the tasks in order. Write a failing JUnit test for the next behavior, make it pass, then refactor. Start with the simple single-threaded version. Choose your own API. Use core Java 21 APIs on the supplied Java 25 toolchain. From `java/`, run `./gradlew :interview-multithreading-ticket-office:test`. The supplied test only checks JUnit and Mockito wiring.

1. **Sell a ticket (6 min).** Start with a nonnegative stock. Sell one ticket and report remaining stock. With one ticket, the first sale succeeds, the second is declined, and the count is zero.
2. **Race for the last ticket (10 min).** Support concurrent callers. With one ticket and two buyers starting together, exactly one receives success. Assert both buyer outcomes as well as the final count.
3. **Sell to groups (12 min).** A buyer may ask for any positive quantity. Fulfil the entire order or decline it without changing stock. With five tickets and two concurrent orders for three, exactly one succeeds and two tickets remain. A single-ticket purchase still competes for the same stock.
4. **Handle a retried purchase (12 min).** A caller may now supply a request ID. All calls with the same ID and quantity receive the first decision, even if they overlap. The quantity is debited at most once. Reusing an ID with a different quantity is a conflict; it cannot change the original decision or stock. This applies to declined orders too: if `r7` was declined for three, retrying `r7` for one conflicts even if one is available. Hold decisions for the lifetime of the office.

**Finish line:** Each purchase must appear to take effect at one instant. Stock never goes negative. Service calls must not wait on a lock, another buyer, or future stock; retrying an atomic update under contention is allowed. After your timed run, compare with [the solution notes](SOLUTION.md).
