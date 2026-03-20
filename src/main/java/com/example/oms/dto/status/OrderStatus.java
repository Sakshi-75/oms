package com.example.oms.dto.status;

public sealed interface OrderStatus permits NewOrder, ProcessingOrder, CompletedOrder, CancelledOrder {
}
