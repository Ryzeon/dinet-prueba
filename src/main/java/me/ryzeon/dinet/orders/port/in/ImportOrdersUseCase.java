package me.ryzeon.dinet.orders.port.in;

import java.io.InputStream;
import java.time.Clock;
import me.ryzeon.dinet.orders.application.dto.OrderLoadSummary;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 01:28</p>
 * <p>Inbound port: bulk import of orders from  CSV file.</p>
 */
public interface ImportOrdersUseCase {

    OrderLoadSummary loadFromCsv(InputStream csvContent, Clock clock);
}
