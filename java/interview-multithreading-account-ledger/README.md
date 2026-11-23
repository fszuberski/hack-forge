# Build It: transfer the last €80

One in-memory ledger holds a fixed set of accounts with nonnegative balances. Use integer cents and assume the total fits in a `long`. There are no deposits, exchange rates, or external payment calls.

**40 minutes, tests first.** Choose your API. Write a failing JUnit test before implementing each new behavior. From `java/`: `./gradlew :interview-multithreading-account-ledger:test`. The starter test exercises JUnit and Mockito only.

1. **Transfer (6 min).** Move a positive amount between distinct known accounts or decline without changing balances. Reject insufficient funds. Offer a balance snapshot. From A=100, B=0, moving 80 produces A=20, B=80.
2. **Prevent overdraft (10 min).** A starts with 100; B and C start empty. Race A→B and A→C for 80 each. Exactly one succeeds; A holds 20 and the recipient of the declined transfer holds zero.
3. **Transfer both ways (12 min).** With A=B=100, race A→B for 60 and B→A for 40. Both must succeed, leaving A=80 and B=120. Any snapshot taken during those calls must total 200; a caller cannot observe half a transfer. No operation may wait on another account or a lock.
4. **Replay a transfer (12 min).** For valid transfer instructions, accept a caller-supplied ID. Identical retries return the first decision, including insufficient funds, even if balances later change. Reusing an ID with different instructions reports conflict. Two concurrent identical requests move the money at most once.

Inspired by [candidate code](https://github.com/micael-vicente/revolut-live-coding) and [review feedback on concurrent transfers](https://github.com/peshrus/revolut-interview-backend-2019#review-result-with-my-comments). This is a core Java exercise separate from the PostgreSQL starter. [Solution notes](SOLUTION.md).
