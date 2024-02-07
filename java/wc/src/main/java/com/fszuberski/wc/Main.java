package com.fszuberski.wc;

import com.fszuberski.wc.adapter.ConsoleInputAdapter;
import com.fszuberski.wc.application.port.in.CountUseCase;
import com.fszuberski.wc.application.service.CountService;

public class Main {
    public static void main(String[] args) {
        // simple di
        final CountUseCase countUseCase = new CountService();
        final ConsoleInputAdapter consoleInputAdapter = new ConsoleInputAdapter(countUseCase);

        // run
        try {
            consoleInputAdapter.readInputAndCount(args);
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }
}