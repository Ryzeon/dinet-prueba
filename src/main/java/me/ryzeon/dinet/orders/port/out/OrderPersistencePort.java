package me.ryzeon.dinet.orders.port.out;

import java.util.List;
import me.ryzeon.dinet.orders.domain.model.OrderLine;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 09:28</p>
 * <p>Persists valid order lines in batch; JPA adapter maps {@link OrderLine} to entities.</p>
 */
public interface OrderPersistencePort {

    void saveAllInBatch(List<OrderLine> validOrders);
}
