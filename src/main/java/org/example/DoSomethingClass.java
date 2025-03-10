package org.example;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

@Slf4j
public class DoSomethingClass {


    public Optional<FinalResponse> doSomething(Integer someNumber, String someString,
                                               String correlationDataToFilter) throws ExecutionException, InterruptedException {

        return createRequestA(someNumber, someString, correlationDataToFilter)
                .thenComposeAsync(this::invokeServiceA)
                .thenComposeAsync(responseA -> createRequestBFromResponseA(correlationDataToFilter, responseA))
                .thenComposeAsync(this::invokeServiceB)
                .thenComposeAsync(contextForServiceB -> getFinalResponse(correlationDataToFilter, contextForServiceB.responseB(), contextForServiceB.requestB()))
                .thenApplyAsync(Optional::ofNullable)
                .get();

    }

    private CompletableFuture<RequestA> createRequestA(Integer someNumber, String someString,
                                                       String correlationDataToFilter) {
        return CompletableFuture.supplyAsync(() -> new RequestA(someNumber, someString,
                new SomeOtherRecord(correlationDataToFilter, Integer.valueOf(200))));
    }

    private CompletableFuture<FinalResponse> getFinalResponse(String correlationDataToFilter, ResponseB responseB, RequestB requestB) {
        return CompletableFuture.supplyAsync(() -> {
                            SomeOtherResponseB someOtherResponseB =
                                    responseB.someOtherResponseB()
                                            .getOrDefault(
                                                    new SomeKey(correlationDataToFilter, requestB.someDouble()),
                                                    null);
                            return Objects.nonNull(someOtherResponseB) ?
                                    new FinalResponse(
                                            requestB.someOtherResponseAs(),
                                            someOtherResponseB
                                    ) : null;
                        }
                );
    }

    private CompletableFuture<RequestB> createRequestBFromResponseA(String correlationDataToFilter, ResponseA responseA) {
        return  CompletableFuture.supplyAsync( () -> responseA.someOtherResponseA().stream()
                .filter(r ->
                        r.someResponseStr().equalsIgnoreCase(correlationDataToFilter))
                .map(r ->
                        new RequestB(
                                r.someBigDecimal().add(r.someOtherBigDecimal()).doubleValue(),
                                new ArrayList<>(Collections.singleton(r))
                        )
                ).reduce((a, b) ->
                        new RequestB(
                                a.someDouble() + b.someDouble(),
                                Stream.concat(a.someOtherResponseAs().stream(),
                                        b.someOtherResponseAs().stream()).toList()
                        )
                ).orElseThrow(() -> new RuntimeException(STR."None of ResposeA s match \{correlationDataToFilter}")));
    }

    private CompletableFuture<ResponseA> invokeServiceA(RequestA requestA) {
        // TODO: write your beautiful Http non-blocking client code
        return CompletableFuture.completedFuture(new ResponseA(
                List.of(
                        new SomeOtherResponseA("FilterString", BigDecimal.ZERO, BigDecimal.ONE),
                        new SomeOtherResponseA("FilterString", BigDecimal.ONE, BigDecimal.TEN),
                        new SomeOtherResponseA("AnotherFilterString", BigDecimal.valueOf(450), BigDecimal.TEN),
                        new SomeOtherResponseA("Non-FilterString", BigDecimal.ZERO, BigDecimal.ZERO)
                )
        ));
    }

    private CompletableFuture<ContextForServiceB> invokeServiceB(RequestB requestB) {
        // TODO: write your beautiful Http non-blocking client code
        return CompletableFuture.completedFuture(new ContextForServiceB(new ResponseB(
                Map.of(
                        new SomeKey("FilterString", 12d), new SomeOtherResponseB("FilterStringMatchedResponse", BigDecimal.valueOf(350)),
                        new SomeKey("AnotherFilterString", 460d), new SomeOtherResponseB("FilterStringAnotherMatchedResponse", BigDecimal.valueOf(116)),
                        new SomeKey("FilterString", 2d), new SomeOtherResponseB("FilterStringNoMatchResponse", BigDecimal.valueOf(226))
                )
        ), requestB));
    }

    public record ContextForServiceB(ResponseB responseB, RequestB requestB) {}



    public record RequestA(Integer someNumber, String someString, SomeOtherRecord someOtherRecord) {}

    public record SomeOtherRecord(String someOtherString, Integer someOtherNumber) {}

    public record ResponseA(List<SomeOtherResponseA> someOtherResponseA){}

    public record SomeOtherResponseA(String someResponseStr,
                                     BigDecimal someBigDecimal, BigDecimal someOtherBigDecimal) {}

    public record RequestB(double someDouble, List<SomeOtherResponseA> someOtherResponseAs) {
    }


    public record ResponseB(Map<SomeKey, SomeOtherResponseB> someOtherResponseB){}
    public record SomeKey(String someStrKey, double someDoubleKey) {}

    public record SomeOtherResponseB(String someString, BigDecimal someBigDecimal) {}

    public record FinalResponse(List<SomeOtherResponseA> someOtherResponseA,
                                SomeOtherResponseB someOtherResponseB) {}
}
