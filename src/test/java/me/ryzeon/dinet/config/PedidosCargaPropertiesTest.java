package me.ryzeon.dinet.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PedidosCargaPropertiesTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @ParameterizedTest
    @ValueSource(ints = {500, 750, 1000})
    void batchSize_inRange_isValid(int size) {
        assertThat(validator.validate(new PedidosCargaProperties(size))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {499, 1001, 1, 10_000})
    void batchSize_outOfRange_isInvalid(int size) {
        assertThat(validator.validate(new PedidosCargaProperties(size))).isNotEmpty();
    }

    @Test
    void defaultMatchesLowerBoundContract() {
        assertThat(validator.validate(new PedidosCargaProperties(500))).isEmpty();
    }
}
