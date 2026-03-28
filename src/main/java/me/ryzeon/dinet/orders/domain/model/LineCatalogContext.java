package me.ryzeon.dinet.orders.domain.model;

import java.util.Optional;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 01:09</p>
 * <p>Snapshot of catalog data needed to validate a line. The application layer loads it through outbound ports before calling the domain validator.</p>
 */
public record LineCatalogContext(boolean customerExists, Optional<Boolean> zoneRefrigerationSupport) {

    public static LineCatalogContext withoutZone(boolean customerExists) {
        return new LineCatalogContext(customerExists, Optional.empty());
    }

    public static LineCatalogContext of(
            boolean customerExists, boolean zoneExists, boolean refrigerationSupported) {
        return new LineCatalogContext(customerExists, zoneExists ? Optional.of(refrigerationSupported) : Optional.empty());
    }
}
