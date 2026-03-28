package me.ryzeon.dinet.orders.adapter.out.persistence;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.ryzeon.dinet.orders.adapter.out.persistence.entity.OrderEntity;
import me.ryzeon.dinet.orders.adapter.out.persistence.mapper.OrderEntityMapper;
import me.ryzeon.dinet.orders.adapter.out.persistence.repository.OrderJpaRepository;
import me.ryzeon.dinet.orders.domain.model.OrderLine;
import me.ryzeon.dinet.orders.port.out.OrderPersistencePort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 10:05</p>
 */
@Component
@RequiredArgsConstructor
public class OrderPersistenceJpaAdapter implements OrderPersistencePort {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderEntityMapper orderEntityMapper;

    @Override
    @Transactional
    public void saveAllInBatch(List<OrderLine> validOrders) {
        if (validOrders.isEmpty()) {
            return;
        }
        List<OrderEntity> entities = validOrders.stream().map(orderEntityMapper::toNewEntity).toList();
        orderJpaRepository.saveAll(entities);
    }
}
