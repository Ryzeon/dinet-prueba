package me.ryzeon.dinet.orders.port.out;

import java.util.Set;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 01:16</p>
 * <p>Outbound port: resolve which customer ids exist (batch-friendly for large imports).</p>
 */
public interface CustomerCatalogPort {

    Set<String> existingIdsAmong(Set<String> customerIds);
}
