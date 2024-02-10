package com.fszuberski.wc.adapter;

import com.fszuberski.wc.application.service.BufferedInputSupplier;
import com.fszuberski.wc.application.domain.CountResult;
import com.fszuberski.wc.application.domain.CountType;
import com.fszuberski.wc.application.port.in.CountUseCase;

import java.io.*;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Supplier;

public class ConsoleInputAdapter {
    private final CountUseCase countUseCase;

    public ConsoleInputAdapter(CountUseCase countUseCase) {
        this.countUseCase = countUseCase;
    }

    public void readInputAndCount(String[] arguments) throws IOException {
        System.out.println("--- Arguments: ");
        Arrays.stream(arguments).forEach(System.out::println);
        System.out.println("---");

        final Supplier<String> inputSupplier;
        final CountResult result;

//        File file = new File("");
//        InputStream inputStream = new FileInputStream(file);
       // temp
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classloader.getResourceAsStream("test.txt");
        if (inputStream == null) {
            throw new RuntimeException("Unable to read file as stream");
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream))) {

            inputSupplier = () -> {
                try {
                    return reader.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            };

            result = countUseCase.count(BufferedInputSupplier.from(inputSupplier), Set.of(CountType.BYTES, CountType.CHARACTERS, CountType.WORDS, CountType.LINES));
        }

        System.out.println(result);
    }
}
