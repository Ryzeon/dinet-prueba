package me.ryzeon.dinet.orders.adapter.out.persistence;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.ryzeon.dinet.orders.adapter.out.persistence.entity.ZoneEntity;
import me.ryzeon.dinet.orders.adapter.out.persistence.repository.ZoneJpaRepository;
import me.ryzeon.dinet.orders.port.out.ZoneCatalogPort;
import org.springframework.stereotype.Component;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 10:05</p>
 */
@Component
@RequiredArgsConstructor
public class ZoneCatalogJpaAdapter implements ZoneCatalogPort {

    private final ZoneJpaRepository zoneJpaRepository;

    @Override
    public Map<String, Boolean> refrigerationSupportFor(Set<String> zoneIds) {
        if (zoneIds == null || zoneIds.isEmpty()) {
            return Map.of();
        }
        return zoneJpaRepository.findAllByIdIn(zoneIds).stream()
                .collect(Collectors.toMap(ZoneEntity::getId, ZoneEntity::isSoporteRefrigeracion));
    }
}
