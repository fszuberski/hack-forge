package com.fszuberski.multithreading.virtualthreads;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

import static com.fszuberski.multithreading.support.TestThreads.await;
import static org.junit.jupiter.api.Assertions.*;

@Tag("exercise")
@Timeout(12)
class BlockingGatewayTest {
    @Test
    void shouldStartAndJoinEveryVirtualThreadGivenBlockingRequests() throws Exception {
        var allEntered = new CountDownLatch(8);
        Set<Thread> workers = ConcurrentHashMap.newKeySet();
        List<Callable<String>> requests = IntStream.range(0, 8).mapToObj(i -> (Callable<String>) () -> {
            workers.add(Thread.currentThread());
            assertTrue(Thread.currentThread().isVirtual());
            allEntered.countDown();
            await(allEntered);
            return "response-" + i;
        }).toList();
        assertEquals(IntStream.range(0, 8).mapToObj(i -> "response-" + i).toList(),
                new BlockingGateway().fetchAll(requests));
        assertEquals(8, workers.size());
        workers.forEach(thread -> assertFalse(thread.isAlive(), "Owned executor must have terminated"));
    }

    @Test
    void shouldReturnEmptyResultsGivenNoRequests() throws Exception {
        assertEquals(List.of(), new BlockingGateway().fetchAll(List.of()));
    }

    @Test
    void shouldPropagateCauseGivenFailedRequest() {
        List<Callable<String>> failed = List.of(() -> { throw new IllegalStateException("HTTP failure"); });
        assertInstanceOf(IllegalStateException.class,
                assertThrows(ExecutionException.class, () -> new BlockingGateway().fetchAll(failed)).getCause());
    }
}
