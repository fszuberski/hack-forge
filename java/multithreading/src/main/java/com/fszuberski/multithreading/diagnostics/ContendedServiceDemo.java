package com.fszuberski.multithreading.diagnostics;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/** An isolated, bounded diagnostic fixture, not a reference exercise solution. */
public final class ContendedServiceDemo {
    private ContendedServiceDemo() {}

    public static void main(String[] args) throws InterruptedException {
        Object monitor = new Object();
        var entered = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        Thread owner = Thread.ofPlatform().name("slow-inventory-owner").unstarted(() -> {
            synchronized (monitor) {
                entered.countDown();
                try {
                    // Observation window only: the demo always releases the monitor eventually.
                    release.await(90, TimeUnit.SECONDS);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        Thread contender = Thread.ofPlatform().name("blocked-inventory-request").unstarted(() -> {
            synchronized (monitor) {
                System.out.println("Request completed after the owner released the monitor.");
            }
        });
        owner.start();
        try {
            if (!entered.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Owner did not acquire the monitor");
            }
            contender.start();
            System.out.println("Demo PID: " + ProcessHandle.current().pid());
            System.out.println("Inspect with: jcmd " + ProcessHandle.current().pid() + " Thread.print -l");
            System.out.println("The contention ends automatically within 90 seconds.");
            owner.join();
            contender.join();
        } finally {
            release.countDown();
            owner.interrupt();
        }
    }
}
