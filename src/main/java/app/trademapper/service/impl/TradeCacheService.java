package app.trademapper.service.impl;

import app.trademapper.model.Trade;
import java.time.Duration;
import java.util.List;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
public class TradeCacheService {
    private final ValueOperations<String, List<Trade>> valueOps;

    public TradeCacheService(RedisTemplate<String, List<Trade>> redisTemplate) {
        this.valueOps = redisTemplate.opsForValue();
    }

    public void saveToCache(String fileHash, List<Trade> trades, Duration ttl) {
        valueOps.set(fileHash, trades, ttl);
    }

    public List<Trade> getFromCache(String fileHash) {
        return valueOps.get(fileHash);
    }
}
