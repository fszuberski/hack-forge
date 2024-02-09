package com.fszuberski.wc.adapter;

import com.fszuberski.wc.application.domain.CountResult;
import com.fszuberski.wc.application.domain.CountType;
import com.fszuberski.wc.application.port.in.CountUseCase;

import java.io.*;
import java.util.Arrays;
import java.util.Optional;
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

        final Supplier<String> bufferedInputSupplier;
        final Optional<CountResult> resultOpt;

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

            bufferedInputSupplier = () -> {
                try {
                    return reader.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            };

            resultOpt = countUseCase.count(bufferedInputSupplier, Set.of(CountType.BYTES, CountType.CHARACTERS, CountType.WORDS, CountType.LINES));
        }

        resultOpt.ifPresentOrElse(
                System.out::println,
                () -> System.out.println("Result is empty")
        );

    }
}
