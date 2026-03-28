package me.ryzeon.dinet.orders.adapter.out.persistence;

import me.ryzeon.dinet.orders.adapter.out.persistence.entity.IdempotencyLoadEntity;
import me.ryzeon.dinet.orders.adapter.out.persistence.repository.IdempotencyLoadJpaRepository;
import me.ryzeon.dinet.orders.port.out.LoadIdempotencyPort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 13:30</p>
 */
@Component
public class LoadIdempotencyPersistenceAdapter implements LoadIdempotencyPort {

    private final IdempotencyLoadJpaRepository repository;

    public LoadIdempotencyPersistenceAdapter(IdempotencyLoadJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean wasAlreadyProcessed(String idempotencyKey, String fileSha256Hex) {
        return repository.findByIdempotencyKeyAndArchivoHash(idempotencyKey, fileSha256Hex).isPresent();
    }

    @Override
    @Transactional
    public void markProcessed(String idempotencyKey, String fileSha256Hex) {
        IdempotencyLoadEntity entity = new IdempotencyLoadEntity();
        entity.setIdempotencyKey(idempotencyKey);
        entity.setArchivoHash(fileSha256Hex);
        try {
            repository.save(entity);
        } catch (DataIntegrityViolationException ignored) {
            // Si pasa data integrity, está bien porque significa que ya se procesó esta combinación de idempotencyKey + fileHash
        }
    }
}
