package com.grupo8.turnos_app.modules.appointment.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.grupo8.turnos_app.modules.appointment.exceptions.TooManyBookingsException;

// guests book without an account, and the unpaid-booking check is per email, so changing the email
// let one person hold every free slot of a business. this caps the bookings a guest can make from
// the same ip inside the window an unpaid booking is held (30 min by default).
// it lives in memory: enough for a single instance, and it resets when the app restarts.
@Component
public class GuestBookingLimiter {

    private final int maxBookings;
    private final Duration window;
    private final Map<String, Deque<Instant>> bookingsByIp = new ConcurrentHashMap<>();

    public GuestBookingLimiter(
            @Value("${app.booking.guest-max-per-ip:3}") int maxBookings,
            @Value("${app.booking.guest-window-minutes:30}") long windowMinutes) {
        this.maxBookings = maxBookings;
        this.window = Duration.ofMinutes(windowMinutes);
    }

    public void checkAllowed(String ip) {
        Deque<Instant> bookings = bookingsByIp.get(ip);
        if (bookings == null)
            return;
        synchronized (bookings) {
            dropExpired(bookings);
            if (bookings.size() >= maxBookings)
                throw new TooManyBookingsException(
                        "Hiciste muchas reservas seguidas. Pagá las que tenés pendientes o probá de nuevo en unos minutos.");
        }
    }

    public void recordBooking(String ip) {
        Deque<Instant> bookings = bookingsByIp.computeIfAbsent(ip, key -> new ArrayDeque<>());
        synchronized (bookings) {
            dropExpired(bookings);
            bookings.addLast(Instant.now());
        }
    }

    // keeps the map from growing with ips that stopped booking
    @Scheduled(fixedRate = 600000)
    public void cleanUp() {
        bookingsByIp.entrySet().removeIf(entry -> {
            synchronized (entry.getValue()) {
                dropExpired(entry.getValue());
                return entry.getValue().isEmpty();
            }
        });
    }

    private void dropExpired(Deque<Instant> bookings) {
        Instant limit = Instant.now().minus(window);
        while (!bookings.isEmpty() && bookings.peekFirst().isBefore(limit))
            bookings.pollFirst();
    }
}
