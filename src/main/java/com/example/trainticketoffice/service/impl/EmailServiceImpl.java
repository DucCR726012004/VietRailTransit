package com.example.trainticketoffice.service.impl;

import com.example.trainticketoffice.model.Booking;
import com.example.trainticketoffice.model.Order;
import com.example.trainticketoffice.model.Payment;
import com.example.trainticketoffice.repository.BookingRepository;
import com.example.trainticketoffice.repository.PaymentRepository;
import com.example.trainticketoffice.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    @Value("${app.mail.from}")
    private String mailFrom;

    @Override
    @Async
    @Transactional
    public void sendBookingConfirmation(Payment payment, List<Order> paidOrders) {
        // Reload payment trong transaction của thread này
        Payment freshPayment = paymentRepository.findById(payment.getPaymentId())
                .orElse(payment);

        // Lấy tất cả booking kèm Trip + Seat (JOIN FETCH) theo các order ID
        List<Long> orderIds = paidOrders.stream()
                .map(Order::getOrderId)
                .toList();
        List<Booking> allBookings = bookingRepository.findByOrderIdsWithDetails(orderIds);

        if (allBookings.isEmpty()) {
            log.warn("Không có booking nào để gửi email xác nhận cho các order: {}", orderIds);
            return;
        }

        // Gom nhóm bookings theo email được điền trong booking
        // Bỏ qua những booking không có email
        Map<String, List<Booking>> bookingsByEmail = allBookings.stream()
                .filter(b -> b.getEmail() != null && !b.getEmail().isBlank())
                .collect(Collectors.groupingBy(Booking::getEmail));

        if (bookingsByEmail.isEmpty()) {
            log.warn("Không có email nào trong các booking của order: {}", orderIds);
            return;
        }

        // Gửi email riêng cho từng địa chỉ email
        bookingsByEmail.forEach((recipientEmail, bookingsForEmail) -> {
            try {
                // Tính tổng tiền của riêng các vé này
                BigDecimal subTotal = bookingsForEmail.stream()
                        .map(Booking::getPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Lấy tên hành khách đầu tiên để chào trong email
                String recipientName = bookingsForEmail.get(0).getPassengerName();

                // Chuẩn bị context cho Thymeleaf
                Context ctx = new Context(new Locale("vi", "VN"));
                ctx.setVariable("payment", freshPayment);
                ctx.setVariable("userName", recipientName);
                ctx.setVariable("totalAmount", subTotal);
                ctx.setVariable("allBookings", bookingsForEmail);

                // Render HTML
                String htmlContent = templateEngine.process("emails/booking-confirmation", ctx);

                // Gửi email
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                helper.setFrom(mailFrom);
                helper.setTo(recipientEmail);
                helper.setSubject("✅ Xác nhận đặt vé tàu - Mã GD #"
                        + (freshPayment.getVnpTransactionNo() != null
                            ? freshPayment.getVnpTransactionNo()
                            : freshPayment.getTransactionRef()));
                helper.setText(htmlContent, true);

                mailSender.send(mimeMessage);
                log.info("Email xác nhận ({} vé) đã gửi thành công đến: {}",
                        bookingsForEmail.size(), recipientEmail);

            } catch (Exception e) {
                log.error("Lỗi khi gửi email xác nhận đến {}: {}", recipientEmail, e.getMessage(), e);
            }
        });
    }
}
