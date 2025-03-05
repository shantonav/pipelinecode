package org.example;

import static java.lang.Integer.valueOf;
import static org.assertj.core.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class DoSomethingTest {

    @Test
    public void testDoSomething() throws ExecutionException, InterruptedException {
        DoSomethingClass doSomething = new DoSomethingClass();
        Optional <DoSomethingClass.FinalResponse> finalResponse =
                doSomething.doSomething(valueOf(123), "SomeInputString",
                 "FilterString");
        assertThat(finalResponse).isPresent();
        assertThat(finalResponse.get().someOtherResponseB()).isNotNull();
        assertThat(finalResponse.get().someOtherResponseB().someBigDecimal()).isEqualTo(BigDecimal.valueOf(350));
        assertThat(finalResponse.get().someOtherResponseB().someString()).isEqualTo("FilterStringMatchedResponse");
    }

    @Test
    public void testAnotherFilteringOnDoSomething() throws ExecutionException, InterruptedException {
        DoSomethingClass doSomething = new DoSomethingClass();
        Optional<DoSomethingClass.FinalResponse> finalResponse =
                doSomething.doSomething(valueOf(123), "SomeInputString",
                        "AnotherFilterString");
        assertThat(finalResponse).isPresent();
        assertThat(finalResponse.get().someOtherResponseB()).isNotNull();
        assertThat(finalResponse.get().someOtherResponseB().someBigDecimal()).isEqualTo(BigDecimal.valueOf(116));
        assertThat(finalResponse.get().someOtherResponseB().someString()).isEqualTo("FilterStringAnotherMatchedResponse");
    }

    @Test
    public void testDoSomethingWhenFinalResponseIsNotThere() throws ExecutionException, InterruptedException {
        DoSomethingClass doSomething = new DoSomethingClass();
        Optional<DoSomethingClass.FinalResponse> finalResponse =
                doSomething.doSomething(valueOf(123), "SomeInputString",
                        "Non-FilterString");
        assertThat(finalResponse).isNotPresent();
    }

    @Test
    public void testExceptionDoSomething()  {
        DoSomethingClass doSomething = new DoSomethingClass();
        ExecutionException excExcp =
                assertThrows(ExecutionException.class, () -> doSomething.doSomething(valueOf(123), "SomeInputString",
                "abcd"));
         assertThat(excExcp.getCause()).isInstanceOf(RuntimeException.class);
        assertThat(excExcp.getCause().getMessage()).isEqualTo("None of ResposeA s match abcd");
    }
}
