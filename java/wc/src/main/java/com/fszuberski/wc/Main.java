package com.fszuberski.wc;

import com.fszuberski.wc.adapter.cli.CommandLineAdapter;
import com.fszuberski.wc.application.port.in.CountUseCase;
import com.fszuberski.wc.application.service.CountService;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        // simple di
        final CountUseCase countUseCase = new CountService();
        final CommandLineAdapter commandLineAdapter = new CommandLineAdapter(countUseCase);

        // run
        commandLineAdapter.readInputAndCount(args);
    }
}