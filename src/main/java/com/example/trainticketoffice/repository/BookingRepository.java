package com.example.trainticketoffice.repository;

import com.example.trainticketoffice.common.BookingStatus;
import com.example.trainticketoffice.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking,Long> {

    List<Booking> findByUser_Id(Integer userId);
    boolean existsByTrip_TripIdAndSeat_SeatIdAndStatusIn(Long tripId, Long seatId, Collection<BookingStatus> statuses);
    List<Booking> findAllByTrip_TripIdAndStatusIn(Long tripId, Collection<BookingStatus> statuses);
    List<Booking> findAllByTrip_TripIdAndStatus(Long tripId, BookingStatus status);

    /**
     * Tải bookings kèm Trip và Seat (JOIN FETCH) để tránh LazyInitializationException
     * khi sử dụng trong @Async context (email service).
     */
    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.trip t " +
           "JOIN FETCH b.seat s " +
           "WHERE b.order.orderId IN :orderIds")
    List<Booking> findByOrderIdsWithDetails(@Param("orderIds") List<Long> orderIds);
}