package com.fszuberski.wc.adapter.cli;

import com.fszuberski.wc.application.service.BufferedInputSupplier;
import com.fszuberski.wc.application.domain.CountResult;
import com.fszuberski.wc.application.port.in.CountUseCase;

import java.io.*;
import java.util.function.Supplier;

public class CommandLineAdapter {
    private final CountUseCase countUseCase;

    public CommandLineAdapter(CountUseCase countUseCase) {
        this.countUseCase = countUseCase;
    }

    public void readInputAndCount(String[] arguments) throws IOException {
        var commandLineArguments = CommandLineArguments.from(arguments);

        final Supplier<String> inputSupplier;
        final CountResult result;
        final File file = new File(commandLineArguments.filePath());
        final InputStream inputStream = new FileInputStream(file);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream))) {

            inputSupplier = () -> {
                try {
                    return reader.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            };

            result = countUseCase.count(BufferedInputSupplier.from(inputSupplier), commandLineArguments.countTypes());
        }

        System.out.println(result);
    }

}
