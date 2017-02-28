package uk.ac.ebi.pride.archive.web.service.interceptor;

import org.springframework.context.support.GenericApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static uk.ac.ebi.pride.archive.web.service.interceptor.RateLimitInterceptor.MAX_REQUESTS_PER_PERIOD;

/**
 * This class implements the RateLimitService interface to limit users' connections.
 * Redis is used as a key/value store. The key is made up of a user's IP plus general request type (i.e. GET),
 * and the value is the current count for the number of requests made in the time period COUNT_EXPIRY_PERIOD_SECONDS.
 * In Redis, Key/value pairs expire after COUNT_EXPIRY_PERIOD_SECONDS from the last increment or initial insert.
 *
 * @author Tobias Ternent
 */
@Service
@EnableScheduling
public class RateLimitServiceImpl extends GenericApplicationContext implements RateLimitService {
  public static final int COUNT_EXPIRY_PERIOD_SECONDS = 30;

  /**
   * This method connects to Redis to track the count of users' total requests within the defined time period.
   * @param userKey the key to use for the user.
   * @param jedisPool the pool for new Redis connections.
   * @return the user's current count of requests within the latest time period.
   */
  @Override
  public int incrementLimit(String userKey, JedisPool jedisPool) {
    int result;
    Jedis jedis = jedisPool.getResource();
    try {
      if (jedis.exists(userKey)) {
        jedis.incr(userKey);
        result = Integer.parseInt(jedis.get(userKey));
      } else {
        jedis.set(userKey, "1");
        result = 1;
      }
      if (result < MAX_REQUESTS_PER_PERIOD){
        jedis.expire(userKey, COUNT_EXPIRY_PERIOD_SECONDS);
      }
    } finally {
      if (jedis != null) {
        jedis.close();
      }
    }
    return result;
  }
}