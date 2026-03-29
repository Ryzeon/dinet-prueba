package me.ryzeon.dinet.orders.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 29/03/26 @ 00:12</p>
 */
class LineCatalogContextTest {

    @Test
    void of_zoneMissing_optionalEmpty() {
        LineCatalogContext ctx = LineCatalogContext.of(true, false, true);
        assertThat(ctx.customerExists()).isTrue();
        assertThat(ctx.zoneRefrigerationSupport()).isEmpty();
    }

    @Test
    void of_zonePresent_carriesRefrigerationFlag() {
        LineCatalogContext ctx = LineCatalogContext.of(true, true, false);
        assertThat(ctx.zoneRefrigerationSupport()).contains(false);
    }

    @Test
    void withoutZone_onlyCustomerFlag() {
        LineCatalogContext ctx = LineCatalogContext.withoutZone(false);
        assertThat(ctx.customerExists()).isFalse();
        assertThat(ctx.zoneRefrigerationSupport()).isEqualTo(Optional.empty());
    }
}
