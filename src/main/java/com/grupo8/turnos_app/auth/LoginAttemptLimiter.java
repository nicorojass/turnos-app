package com.grupo8.turnos_app.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.grupo8.turnos_app.common.exception.TooManyAttemptsException;

// without a limit a password could be guessed by brute force. failed logins are counted per email and
// ip together (someone trying passwords on one account) and per ip (someone trying many accounts) inside
// a window. counting per email alone let anyone lock any account (even the admin's) by sending five
// wrong passwords with its email: now that only blocks the ip that sent them.
// a successful login clears the count of that email from that ip. in memory: enough for a single instance.
@Component
public class LoginAttemptLimiter {

    private final int maxPerEmail;
    private final int maxPerIp;
    private final Duration window;
    private final Map<String, Deque<Instant>> failures = new ConcurrentHashMap<>();

    public LoginAttemptLimiter(
            @Value("${app.login.max-failures-per-email:5}") int maxPerEmail,
            @Value("${app.login.max-failures-per-ip:20}") int maxPerIp,
            @Value("${app.login.window-minutes:15}") long windowMinutes) {
        this.maxPerEmail = maxPerEmail;
        this.maxPerIp = maxPerIp;
        this.window = Duration.ofMinutes(windowMinutes);
    }

    public void checkAllowed(String email, String ip) {
        if (count(emailKey(email, ip)) >= maxPerEmail || count(ipKey(ip)) >= maxPerIp)
            throw new TooManyAttemptsException(
                    "Demasiados intentos fallidos. Esperá unos minutos antes de volver a intentar.");
    }

    public void recordFailure(String email, String ip) {
        add(emailKey(email, ip));
        add(ipKey(ip));
    }

    public void recordSuccess(String email, String ip) {
        failures.remove(emailKey(email, ip));
    }

    @Scheduled(fixedRate = 600000)
    public void cleanUp() {
        failures.entrySet().removeIf(entry -> {
            synchronized (entry.getValue()) {
                dropExpired(entry.getValue());
                return entry.getValue().isEmpty();
            }
        });
    }

    private int count(String key) {
        Deque<Instant> attempts = failures.get(key);
        if (attempts == null)
            return 0;
        synchronized (attempts) {
            dropExpired(attempts);
            return attempts.size();
        }
    }

    private void add(String key) {
        Deque<Instant> attempts = failures.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (attempts) {
            dropExpired(attempts);
            attempts.addLast(Instant.now());
        }
    }

    private void dropExpired(Deque<Instant> attempts) {
        Instant limit = Instant.now().minus(window);
        while (!attempts.isEmpty() && attempts.peekFirst().isBefore(limit))
            attempts.pollFirst();
    }

    private static String emailKey(String email, String ip) {
        return "email:" + (email == null ? "" : email.trim().toLowerCase(Locale.ROOT)) + "|ip:" + ip;
    }

    private static String ipKey(String ip) {
        return "ip:" + ip;
    }
}
