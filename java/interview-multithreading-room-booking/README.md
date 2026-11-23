# Build It: one room, two guests

Build the in-memory calendar for **one** hotel room. Guests request stays by check-in and checkout date. There is no clock, availability search, or other room to fall back on. Each new booking attempt carries a fresh caller-supplied booking ID; IDs are never recycled for a different attempt.

**40-minute TDD drill.** Take one task at a time: write a failing JUnit test, make it pass, then refactor. Choose your own API. Use core Java 21 APIs on the supplied Java 25 toolchain. From `java/`, run `./gradlew :interview-multithreading-room-booking:test`. The supplied test only checks JUnit and Mockito wiring.

1. **Book sequentially (6 min).** Accept a stay only if check-in precedes checkout and no accepted stay overlaps it. Reject invalid or conflicting stays without changing the calendar. A guest leaving on June 12 and one arriving on June 12 can both book.
2. **Handle competing guests (10 min).** For concurrent requests on an empty calendar for June 10–13 and June 12–15, exactly one wins. For June 10–12 and June 12–15, both win. A rejection must correspond to an actual conflict.
3. **Cancel by ID (12 min).** Report success only when an active booking was removed. If two callers cancel the same booking, one succeeds. Once another guest books those dates, a delayed cancellation of the old ID must leave the new guest's stay intact.
4. **Move a stay without losing it (12 min).** Reschedule an active ID to a new valid interval as one operation. The new stay may overlap its own old dates. Unknown ID, invalid dates, or a clash with another stay means failure with no change. Race a move from June 1–3 to June 8–10 against a fresh booking for June 8–10. Exactly one wins; if the move loses, the original guest still has June 1–3.

**Finish line:** Every booking, cancellation, and move must appear to take effect at one instant. No overlapping guests, temporarily surrendered rooms, or service calls waiting on locks or other guests. Retrying an atomic update is allowed. After the drill, read [the solution notes](SOLUTION.md).
