package me.ryzeon.dinet.orders.adapter.out.persistence.repository;

import java.util.Collection;
import java.util.List;
import me.ryzeon.dinet.orders.adapter.out.persistence.entity.ZoneEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 10:04</p>
 */
@Repository
public interface ZoneJpaRepository extends JpaRepository<ZoneEntity, String> {

    List<ZoneEntity> findAllByIdIn(Collection<String> ids);
}
