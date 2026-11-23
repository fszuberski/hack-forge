# Build It: the closing bid

Run one in-memory auction. Start open with a price of zero and no winner. Bids are positive integer cents, and bidders have distinct identities. Closing is an explicit call; there is no timer.

**40 minutes, tests first.** Choose your API. Write a failing JUnit test for each new behavior. From `java/`: `./gradlew :interview-multithreading-auction:test`. The starter test exercises JUnit and Mockito only.

1. **Bid (6 min).** Accept only amounts strictly above the current price. Keep the leading bidder and amount together. If Ada leads at 100, Bea's bid of 100 is declined and Ada still leads.
2. **Handle competing bids (10 min).** Race 110 and 120 against a leader at 100. The 120 bid must succeed and lead; the 110 bid may have succeeded earlier. For competing equal bids above the current price, exactly one succeeds.
3. **Close (12 min).** Close once, returning the winning bidder and price, or no winner if empty. All subsequent closes return that same result. Race a bid of 120 against closing at 100: accepting 120 and declaring the old winner is forbidden.
4. **Replay (12 min).** Add bid IDs. Retrying the same ID, bidder, and amount returns its original accepted/declined decision, even after close. “Accepted” means it led at the time, not that it ultimately won. Reusing an ID with different details is a conflict. New bids on a closed auction are declined.

Original practice variant. All final operations must take effect at one instant and run without waiting on locks or other bidders. [Solution notes](SOLUTION.md).
