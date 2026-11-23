# Build It: ten backends

Build an in-memory load balancer. A backend is just an address string; selecting it does not make a network call. Assume nonblank address inputs and compare them by exact string equality. The registry starts empty.

**40-minute TDD drill.** Read the tasks in order. Start with a failing JUnit test for one sequential behavior; add new tests as each requirement arrives. Choose your own API. Use core Java 21 APIs on the supplied Java 25 toolchain. From `java/`, run `./gradlew :interview-multithreading-load-balancer:test`. The supplied test only checks JUnit and Mockito wiring.

1. **Build the registry (6 min).** Register up to ten distinct addresses in registration order. A duplicate or eleventh address returns failure with no change. Select a registered address at random, or report empty. With just `A`, every pick returns `A`.
2. **Race for slot ten (10 min).** With nine addresses, concurrently register `X` and `Y`. Exactly one succeeds. Also try registering the same new address from two threads: exactly one succeeds, and the address occupies only one slot.
3. **Remove live backends (12 min).** Deregistration succeeds only for a present address; survivors retain their order. Selection racing with removal of the only backend `A` may return `A` or empty. A selection started after removal finishes must return empty. No selection may fail with an indexing error.
4. **Offer round-robin selection (12 min).** Keep random selection available. Round robin starts with the first registered address and cycles in order. With a fixed `[A, B, C]`, 30 concurrent round-robin calls must return ten of each. A successful membership change restarts the cycle at the first surviving address in registration order (new entries append). Failed changes and random picks do not advance or reset the cycle.

**Finish line:** Each call must appear to act on one consistent registry at one instant. Reads and writes must not wait on a lock or another caller; retrying an atomic update is allowed. After the drill, read [the solution notes](SOLUTION.md).

**Public interview context:** A [candidate-published Revolut problem statement](https://github.com/pulkitent/revolut-load-balancer#problem-statement) describes incremental requirements, in-memory storage, ten unique addresses, and random selection. A [second live-coding account](https://github.com/natalialopesmoreno/live-coding-revolut) discusses thread safety and round robin. The four stages and exact race contracts here are an original practice variant.
