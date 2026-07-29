package com.tableorder.service;

import com.tableorder.dto.response.CashierOrderListResponse;
import com.tableorder.dto.response.ConfirmPaymentResponse;
import com.tableorder.dto.response.MarkSyncedResponse;
import com.tableorder.dto.response.UpdateStatusResponse;

public interface CashierService {
    CashierOrderListResponse listActiveOrders();
    ConfirmPaymentResponse confirmPayment(String orderCode, String confirmedBy);
    MarkSyncedResponse markSynced(String orderCode, String markedBy);
    UpdateStatusResponse updateStatus(String orderCode, String newStatus);
}
