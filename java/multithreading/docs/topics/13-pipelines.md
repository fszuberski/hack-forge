# 13 · Producer–consumer, backpressure, and batching

## Theory

A pipeline separates stages with queues. Bounded queues provide backpressure:
a slow downstream stage eventually blocks/throttles upstream rather than letting
memory grow without bound. Capacity absorbs bursts but does not fix sustained
overload. Define rejection, timeout, dropping, or blocking policy deliberately.

Queue insertion/removal provides a publication boundary for handed-off objects;
don't mutate an item afterward without another protocol. End-of-stream is a control
message, not an arbitrary ordinary value. Multiple consumers require an explicit
termination strategy: one poison pill may stop only one consumer. Out-of-band close
also needs a way to wake blocked participants.

Failure propagation is as important as the happy path. If the transformer dies,
the producer may be blocked on put while the collector is blocked on take. A
supervisor must cancel all stages; a poison pill cannot be reliably inserted into
a full queue after a consumer has stopped. Keep shutdown/cancellation channels
independent of ordinary data flow.

Batching amortizes downstream overhead but can increase latency. Bound batch size,
publish detached batches, flush the final partial batch, and distinguish temporary
emptiness from producer completion. `drainTo` is useful but not a global transaction
with concurrent producers.

## Exercise A · Three-stage bounded pipeline

Implement `pipelines/ProcessingPipeline.java`; run `*ProcessingPipelineTest`.

- Create one producer, one transformer, and one collector, connected by **two
  bounded BlockingQueues** with the supplied positive capacity. The collector may
  run on the calling thread if cancellation supervision remains correct.
- Transform every non-null input exactly once on success and preserve input order.
  Assume non-null transform results. Return the collected list.
- Support empty input and all ordinary input values. Use tagged messages/control
  envelopes, not magic data values like -1 or zero, for end-of-stream.
- If transformation fails, cancel/wake all participants and propagate
  ExecutionException with the original cause. Caller interruption must also cancel
  owned work and propagate. Join/terminate every owned stage on every exit.
- No whole-input copy into an unbounded intermediate queue and no sequential
  implementation disguised behind a queue. The returned output list is allowed
  to grow with input size; only inter-stage work backlog is bounded.

## Exercise B · Batch and drain gracefully

Implement `pipelines/BatchingProcessor.java`; run `*BatchingProcessorTest`.

- One consumer runs on the caller's thread; the caller owns the supplied queue.
- Deliver non-empty, immutable, detached batches of at most positive maxBatch
  items to sink, preserving queue order. Don't wait to fill a complete batch when
  data is already available; drain currently available items up to the limit.
- Return only after producersDone is true **and** the queue is empty. The producer
  contract is: publish every item before setting an appropriately visible done
  flag, and never publish again afterward. There is only one batch consumer.
- Because done changes outside the queue, use bounded timed poll (for example
  25 ms) to recheck completion; don't busy-spin or block forever in take.
- Propagate interruption and sink runtime exceptions. Don't re-enqueue/retry a
  partially delivered batch implicitly, and don't spawn threads.

## Hints

<details><summary>First nudge</summary>
Draw each queue wait on success, empty input, failure, and caller cancellation.
Every wait needs a corresponding wakeup/cancellation path.
</details>

<details><summary>More specific</summary>
For A use a supervisor/failure signal separate from payload messages and retain
handles for interrupting every blocked stage. For B poll for the first item, then
drainTo a fresh list with maxBatch-1, and pass List.copyOf to the sink. Recheck
done/empty in an order that cannot miss the producers' final publication.
</details>

## Interview

1. Why can a larger queue worsen tail latency without improving throughput?
2. How does cancellation reach a producer blocked on a full queue after failure?
3. When does poison-pill shutdown fail with multiple consumers?
4. What delivery guarantees remain if a batch sink partially commits then throws?
