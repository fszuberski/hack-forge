# 11 · Fork/join and parallel streams

## Theory

ForkJoinPool uses work stealing: workers split tasks and steal available work.
It is designed for decomposable CPU-bound work. Avoid blocking I/O or unmanaged
waits in fork/join tasks; compensation is not an unlimited resource guarantee.
Choose a cutoff large enough to amortize task overhead. More tiny tasks can be
slower than a sequential loop. Use an explicit pool when the API exposes one;
common-pool contention from unrelated features can otherwise be surprising.

Parallel stream functions must be non-interfering and generally stateless.
Reductions need an associative operation and compatible identity/combiner; floating
point addition can give different rounding by grouping. `forEach` is not ordered,
and writing to a shared HashMap inside it is unsafe. A collector can use isolated
per-partition containers and safe merging, or a concurrent accumulator with the
appropriate characteristics.

Encounter order, merge cost, data splitting quality, workload size, and CPU capacity
determine whether parallelism pays off. Parallel streams don't expose a general
executor parameter; don't promise pool isolation based on undocumented behavior.
Nested parallelism and blocking calls make capacity and latency harder to reason about.

## Exercise A · Parallel long reduction

Implement `parallelism/ParallelSum.java`; run `*ParallelSumTest`.

- Use `RecursiveTask<Long>` on the provided ForkJoinPool. The caller owns it;
  never shut it down and do not silently switch to the common pool.
- Split disjoint half-open ranges until size <= positive threshold; sum locally.
- Empty input returns zero; use long arithmetic to avoid int accumulation overflow.
- Do not copy subarrays or mutate input. Fork one side, compute the other, then join.
- The contract is CPU-bound and synchronous; it does not promise prompt cancellation
  of recursive tasks on caller interruption. Explain that limitation.

## Exercise B · Repair a parallel stream aggregation

Implement `parallelism/ParallelGrouping.java`; run `*ParallelGroupingTest`.

The broken approach is:

```java
var counts = new java.util.HashMap<String, Long>();
values.parallelStream().forEach(word -> counts.merge(word, 1L, Long::sum));
```

- Replace it with a proper parallel stream reduction/collector, not a synchronized
  block around the whole operation or a sequential stream.
- Return exact per-word long counts for non-null values; empty input gives an empty
  map. No shared mutable state escapes between invocations; don't modify input.
- Explain why a non-concurrent collector can still be safe in a parallel stream
  when each worker owns its partial accumulation container.

Compare the design against groupingByConcurrent; high key skew can shift contention.
Do not assert elapsed-time speedups in the correctness test.

## Hints

<details><summary>First nudge</summary>
Disjoint input ranges and local accumulators minimize sharing. For streams, ask
whether state belongs to a partition or to all workers at once.
</details>

<details><summary>More specific</summary>
For A use pool.invoke on the root task. For B use groupingBy/counting with safe
combination, or groupingByConcurrent/counting with the collector's concurrency protocol.
</details>

## Interview

1. Why can a parallel stream be slower than a loop?
2. What properties must a reduction's identity and combiner have?
3. Is a thread-safe accumulator sufficient if the reduction is non-associative?
4. What workload would you keep out of the common ForkJoinPool?
