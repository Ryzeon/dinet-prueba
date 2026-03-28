package me.ryzeon.dinet.orders.application.csv;

import java.util.List;
import java.util.Optional;
import me.ryzeon.dinet.orders.domain.model.OrderLine;
import me.ryzeon.dinet.orders.domain.validation.OrderLoadErrorCode;

/**
 * <p>Created by Alex Avila Asto - A.K.A (Ryzeon)</p>
 * <p>Project: dinet-prueba</p>
 * <p>Date: 28/03/26 @ 01:30</p>
 * <p>Result of parsing one CSV data line (excluding header). Either an {@link OrderLine} or a non-empty list of
 * parse-time error codes.</p>
 */
public record ParsedCsvRow(int lineNumber, Optional<OrderLine> orderLine, List<OrderLoadErrorCode> parseErrors) {

    public ParsedCsvRow {
        parseErrors = List.copyOf(parseErrors);
    }

    public boolean success() {
        return orderLine.isPresent() && parseErrors.isEmpty();
    }
}
