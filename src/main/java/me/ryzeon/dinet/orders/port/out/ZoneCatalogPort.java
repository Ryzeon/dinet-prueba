package me.ryzeon.dinet.orders.port.out;

import java.util.Map;
import java.util.Set;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 01:16</p>
 * <p>Outbound port: zones by id. Missing key means the zone does not exist. Value is {@code
 * soporte_refrigeracion}.</p>
 */
public interface ZoneCatalogPort {

    Map<String, Boolean> refrigerationSupportFor(Set<String> zoneIds);
}
