package app.trademapper.service.impl;

import app.trademapper.util.ProductDataLoader;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductCacheService {
    private static final String REDIS_KEY_PREFIX = "product:";
    private final StringRedisTemplate redisTemplate;
    private final ProductDataLoader productDataLoader;

    public String getProductName(int productId) {
        String productName = redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + productId);
        return productName != null ? productName : "Unknown Product";
    }

    @PostConstruct
    public void loadProductData() {
        productDataLoader.loadProductsIntoCache(redisTemplate, REDIS_KEY_PREFIX);
    }
}
