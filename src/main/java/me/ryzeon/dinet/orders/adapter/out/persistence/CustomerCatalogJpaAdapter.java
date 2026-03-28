package me.ryzeon.dinet.orders.adapter.out.persistence;

import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.ryzeon.dinet.orders.adapter.out.persistence.entity.CustomerEntity;
import me.ryzeon.dinet.orders.adapter.out.persistence.repository.CustomerJpaRepository;
import me.ryzeon.dinet.orders.port.out.CustomerCatalogPort;
import org.springframework.stereotype.Component;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 10:05</p>
 */
@Component
@RequiredArgsConstructor
public class CustomerCatalogJpaAdapter implements CustomerCatalogPort {

    private final CustomerJpaRepository customerJpaRepository;

    @Override
    public Set<String> existingIdsAmong(Set<String> customerIds) {
        if (customerIds == null || customerIds.isEmpty()) {
            return Set.of();
        }
        return customerJpaRepository.findAllByIdInAndActivoTrue(customerIds).stream()
                .map(CustomerEntity::getId)
                .collect(Collectors.toSet());
    }
}
