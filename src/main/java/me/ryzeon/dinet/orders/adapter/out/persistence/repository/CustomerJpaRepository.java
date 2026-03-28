package me.ryzeon.dinet.orders.adapter.out.persistence.repository;

import java.util.Collection;
import java.util.List;
import me.ryzeon.dinet.orders.adapter.out.persistence.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 10:01</p>
 */
@Repository
public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, String> {

    List<CustomerEntity> findAllByIdInAndActivoTrue(Collection<String> ids);
}
