package me.ryzeon.dinet.orders.adapter.out.persistence.repository;

import java.util.UUID;
import me.ryzeon.dinet.orders.adapter.out.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 10:03</p>
 */
@Repository
public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {}
