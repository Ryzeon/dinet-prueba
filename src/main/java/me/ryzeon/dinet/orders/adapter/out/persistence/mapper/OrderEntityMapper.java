package me.ryzeon.dinet.orders.adapter.out.persistence.mapper;

import me.ryzeon.dinet.orders.adapter.out.persistence.entity.OrderEntity;
import me.ryzeon.dinet.orders.domain.model.OrderLine;
import org.springframework.stereotype.Component;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 10:06</p>
 */
@Component
public class OrderEntityMapper {

    public OrderEntity toNewEntity(OrderLine line) {
        var e = new OrderEntity();
        e.setNumeroPedido(line.orderNumber().trim());
        e.setClienteId(line.customerId().trim());
        e.setZonaId(line.deliveryZoneId().trim());
        e.setFechaEntrega(line.deliveryDate());
        e.setEstado(line.status().name());
        e.setRequiereRefrigeracion(line.requiresRefrigeration());
        return e;
    }
}
