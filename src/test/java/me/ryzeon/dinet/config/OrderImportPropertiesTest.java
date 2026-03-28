package me.ryzeon.dinet.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 01:32</p>
 */
class OrderImportPropertiesTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @ParameterizedTest
    @ValueSource(ints = {500, 750, 1000})
    void batchSize_inRange_valid(int size) {
        assertThat(validator.validate(new OrderImportProperties(size))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {499, 1001, 1, 10_000})
    void batchSize_outOfRange_invalid(int size) {
        assertThat(validator.validate(new OrderImportProperties(size))).isNotEmpty();
    }

    @Test
    void lowerBoundExample_valid() {
        assertThat(validator.validate(new OrderImportProperties(500))).isEmpty();
    }
}
