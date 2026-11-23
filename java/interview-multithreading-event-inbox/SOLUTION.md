# Debrief: who owns this event?

“Event 7 exists” is not enough information to process event 7 safely. The inbox must remember whether it is ready, who currently owns it, and whether the ID has already completed.

## First green test

An ID-to-payload map can answer the sequential submission and readiness questions. A tempting claim implementation is:

```java
Event event = ready.get(id);
if (event == null) return Optional.empty();
ready.remove(id);
return Optional.of(new Claim(event, new Object()));
```

A and B both read event 7. A removes it; B's removal now does nothing, but B also returns a claim. Checking the removal result would improve that race; it wouldn't record ownership, so the inbox still could not safely acknowledge or release a particular delivery.

## Treat claim and release as state transitions

Use one atomic reference to an immutable ID-to-entry map. Each entry retains its original payload and is one of `Ready`, `Claimed(token)`, or `Done`. For this small drill scanning the map for **any** ready event is sufficient. A claim token can be a fresh object whose identity is never reused.

| Call | Precondition on the version read | Replacement |
|---|---|---|
| Submit | ID absent | Ready with the supplied payload |
| Claim | Some entry ready | Claimed with a fresh token |
| Acknowledge | ID claimed by this token | Done; keep the ID as a duplicate marker |
| Release | ID claimed by this token | Ready with the original payload |

Publish the copy with compare-and-set. Return success (and the claim, if applicable) only when publication succeeds. Recheck the precondition after every failed publication; an empty claim result takes effect at a read with no ready entry. This design copies or scans the map, but a paused worker holds no lock. Keeping done IDs costs memory; deleting one would change the promised lifetime of deduplication.

The token prevents an old delivery being mistaken for a new one: after A releases and B reclaims, the event ID and payload match A's old claim, but the token does not. If release and acknowledgement race on a live token, just one transition can publish; retrying the loser cannot act on the old ownership.

**Test to keep:** submit → claim A → release A → claim B → acknowledge A (false) → acknowledge B (true) → resubmit (duplicate). This *sequential* test forces the exact stale-worker history without relying on scheduler luck. Add a two-worker race separately to check that only one claim is handed out.
