# Build It: the last short code

You run one in-memory URL shortener. A short URL identifies exactly one original string for the lifetime of the service. Use a fixed short domain; no network requests or URL canonicalization.

**40 minutes, tests first.** Choose your API. Work in order, writing a failing JUnit test before implementing each behavior. From `java/`: `./gradlew :interview-multithreading-url-pool:test`. The starter test exercises JUnit and Mockito only.

1. **Shorten (7 min).** Accept absolute HTTP(S) URLs with a host. Reject malformed input. Shorten the same string twice and return the same short URL. Different strings get different codes. Store up to 100 mappings: once full, reject new URLs but still serve repeats.
2. **Resolve (6 min).** Given a short URL you returned, recover the exact original. Report unknown for an unassigned short URL. Round-trip two different URLs.
3. **Allocate from a pool (9 min).** Replace generated codes with a supplied ordered list of distinct codes. Use each at most once, in order, maintaining the 100-mapping cap. An invalid input or repeat consumes nothing. An empty pool cannot accept a new URL.
4. **Race for the last code (18 min).** With one code left, concurrent requests for *different* new URLs yield one success and one exhaustion. Requests for the *same* new URL both succeed with the same code. After a success returns, reverse lookup must already work. No caller waits on a lock or for more codes.

Based on a [candidate's three-stage URL-shortener account](https://github.com/DivnychAndrii/url-shortener-interview-oct2025#readme); task 4 is a practice extension. [Solution notes](SOLUTION.md).
