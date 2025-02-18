package app.trademapper.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProductDataLoader {
    private static final String PRODUCT_FILE_PATH = "product.csv";

    public void loadProductsIntoCache(StringRedisTemplate redisTemplate,
                                      String redisKeyPrefix) {
        if (isDataAlreadyLoaded(redisTemplate, redisKeyPrefix)) {
            return;
        }
        List<String[]> products = readProductData();
        if (products.isEmpty()) {
            log.error("No valid product data found.");
            return;
        }
        saveProductsToRedis(redisTemplate, redisKeyPrefix, products);
        markDataAsLoaded(redisTemplate, redisKeyPrefix);
    }

    private boolean isDataAlreadyLoaded(StringRedisTemplate redisTemplate,
                                        String redisKeyPrefix) {
        if (redisKeyPrefix == null || Boolean.TRUE.equals(
                redisTemplate.hasKey(redisKeyPrefix + "loaded"))) {
            System.out.println("Products are already loaded into Redis"
                    + " or invalid key prefix. Skipping.");
            return true;
        }
        return false;
    }

    private List<String[]> readProductData() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource(PRODUCT_FILE_PATH)
                        .getInputStream(), StandardCharsets.UTF_8))) {

            return reader.lines()
                    .skip(1)
                    .map(line -> line.split(","))
                    .filter(cols -> cols.length >= 2)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error reading product file: " + e.getMessage());
            return List.of();
        }
    }

    private void saveProductsToRedis(StringRedisTemplate redisTemplate,
                                     String redisKeyPrefix, List<String[]> products) {
        redisTemplate.executePipelined((RedisCallback<Void>) connection -> {
            for (String[] cols : products) {
                int productId;
                try {
                    productId = Integer.parseInt(cols[0]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid product ID: " + cols[0]);
                    continue;
                }

                String productName = cols[1].trim();
                byte[] key = redisTemplate
                        .getStringSerializer().serialize(redisKeyPrefix + productId);
                byte[] value = redisTemplate.getStringSerializer().serialize(productName);

                if (key != null && value != null) {
                    connection.stringCommands().set(key, value);
                } else {
                    log.error("Skipping null key/value for product: " + productId);
                }
            }
            return null;
        });

        System.out.println("Products were loaded to Redis");
    }

    private void markDataAsLoaded(StringRedisTemplate redisTemplate, String redisKeyPrefix) {
        redisTemplate.opsForValue().set(redisKeyPrefix + "loaded", "true");
    }
}
