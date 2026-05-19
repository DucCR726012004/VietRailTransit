package com.example.trainticketoffice.service;

import com.example.trainticketoffice.model.Order;
import com.example.trainticketoffice.model.Payment;

import java.util.List;

public interface EmailService {

    /**
     * Gửi email xác nhận đặt vé sau khi thanh toán thành công.
     *
     * @param payment     Thông tin giao dịch thanh toán
     * @param paidOrders  Danh sách các đơn hàng đã được thanh toán
     */
    void sendBookingConfirmation(Payment payment, List<Order> paidOrders);
}
