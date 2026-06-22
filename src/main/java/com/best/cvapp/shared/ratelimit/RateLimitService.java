package com.best.cvapp.shared.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    // one bucket per IP address
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String ip, int requests, int seconds) {
        return buckets.computeIfAbsent(ip, key -> newBucket(requests, seconds));
    }

    private Bucket newBucket(int requests, int seconds) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(requests)
                .refillGreedy(requests, Duration.ofSeconds(seconds))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}