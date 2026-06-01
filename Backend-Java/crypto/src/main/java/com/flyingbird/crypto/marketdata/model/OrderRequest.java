package com.flyingbird.crypto.marketdata.model;

import lombok.Builder;
import lombok.Data;

/**
 * Order Request (bracket order model)
 *
 * Migrated from the Python `OrderRequest` dataclass (dtos/order_request.py),
 * limited to the fields actually used by the crossover signal path.
 *
 * In the current flow this object is built on a signal and included in the
 * notification email (the Python crossover path emails the order; it does not
 * place it). Placing orders on Delta is out of scope for this migration phase.
 */
@Data
@Builder
public class OrderRequest {

    private String productId;
    private Integer size;
    private Boolean reduceOnly;
    private String orderType;       // "market_order"
    private String clientOrderId;
    private String side;            // "buy" | "sell"
    private String bracketStopLossPrice;
    private String bracketTakeProfitPrice;
}
