package com.jackpot.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
public class JackpotPoolManager {

    private static final Logger log = LoggerFactory.getLogger(JackpotPoolManager.class);
    private static final String KEY_PREFIX = "jackpot:pool:";

    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<Long> compareAndAddScript;

    public JackpotPoolManager(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.compareAndAddScript = new DefaultRedisScript<>();
        this.compareAndAddScript.setScriptText("local current = redis.call('GET', KEYS[1])\n" +
            "if not current then return -1 end\n" +
            "if current == ARGV[1] then\n" +
            "  local updated = tonumber(current) + tonumber(ARGV[2])\n" +
            "  redis.call('SET', KEYS[1], tostring(updated))\n" +
            "  return 1\n" +
            "end\n" +
            "return 0");
        this.compareAndAddScript.setResultType(Long.class);
    }

    public long readPoolCents(String jackpotId, long defaultValue) {
        String key = key(jackpotId);
        String current = stringRedisTemplate.opsForValue().get(key);
        if (current == null) {
            stringRedisTemplate.opsForValue().set(key, Long.toString(defaultValue));
            return defaultValue;
        }
        return Long.parseLong(current);
    }

    public boolean tryAddContribution(String jackpotId, long expectedCents, long contributionCents) {
        Long result = stringRedisTemplate.execute(
            compareAndAddScript,
            List.of(key(jackpotId)),
            Long.toString(expectedCents),
            Long.toString(contributionCents));
        if (Objects.equals(result, -1L)) {
            log.warn("Pool key missing for jackpotId={} - recreating", jackpotId);
            stringRedisTemplate.opsForValue().set(key(jackpotId), Long.toString(expectedCents));
            return false;
        }
        return Objects.equals(result, 1L);
    }

    public long resetPool(String jackpotId, long newValueCents) {
        stringRedisTemplate.opsForValue().set(key(jackpotId), Long.toString(newValueCents));
        return newValueCents;
    }

    public BigDecimal centsToAmount(long cents) {
        return BigDecimal.valueOf(cents).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public long amountToCents(BigDecimal amount) {
        BigDecimal scaled = amount.setScale(2, RoundingMode.HALF_UP);
        return scaled.multiply(BigDecimal.valueOf(100)).longValueExact();
    }

    private String key(String jackpotId) {
        return KEY_PREFIX + jackpotId;
    }
}
