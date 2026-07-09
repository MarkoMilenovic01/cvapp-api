package com.best.cvapp.auth.oauth;

import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OAuthCodeService {
    private record Entry(Long userId, Instant expiresAt) {}
    private final Map<String, Entry> codes = new ConcurrentHashMap<>();
    private static final long TTL_SECONDS = 60;

    public String createCode(Long userId) {
        cleanup();
        String code = UUID.randomUUID().toString();
        codes.put(code, new Entry(userId, Instant.now().plusSeconds(TTL_SECONDS)));
        return code;
    }

    public Long consumeCode(String code) {
        Entry entry = codes.remove(code); // remove = single use
        if (entry == null || entry.expiresAt().isBefore(Instant.now())) {
            return null;
        }
        return entry.userId();
    }

    private void cleanup() {
        codes.entrySet().removeIf(e -> e.getValue().expiresAt().isBefore(Instant.now()));
    }
}