package me.ryzeon.dinet.orders.adapter.out.persistence.repository;

import java.util.Optional;
import java.util.UUID;
import me.ryzeon.dinet.orders.adapter.out.persistence.entity.IdempotencyLoadEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 13:30</p>
 */
public interface IdempotencyLoadJpaRepository extends JpaRepository<IdempotencyLoadEntity, UUID> {

    Optional<IdempotencyLoadEntity> findByIdempotencyKeyAndArchivoHash(String idempotencyKey, String archivoHash);
}
