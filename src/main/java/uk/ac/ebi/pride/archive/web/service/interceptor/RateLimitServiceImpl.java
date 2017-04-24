package uk.ac.ebi.pride.archive.web.service.interceptor;

import org.springframework.context.support.GenericApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.Instant;

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
  public static final int COUNT_EXPIRY_PERIOD_SECONDS = 15;

  /**
   * This method connects to Redis to track the count of users' total requests within the defined time period.
   * @param userKey the key to use for the user.
   * @param jedisPool the pool for new Redis connections.
   * @return the user's current count of requests within the latest time period.
   */
  @Override
  public int incrementLimit(String userKey, JedisPool jedisPool) throws Exception {
    int result = 0;
    Jedis jedis = null;
    try {
      jedis = jedisPool.getResource();
      if (jedis.exists(userKey)) {
        logger.debug("Jedis user key exists, has been flagged for rate limit");
        result = Integer.parseInt(jedis.get(userKey)); // user has already been flagged at the limit
      } else {
        long currentTimeStampSeconds = Instant.now().getEpochSecond();
        String timedUserKey = userKey + ":" + currentTimeStampSeconds;
        for (int i=1; i<=COUNT_EXPIRY_PERIOD_SECONDS; i++) {
          String keyToTry = userKey + ":" + (currentTimeStampSeconds - i);
          if (jedis.exists(keyToTry)) {
            result += Integer.parseInt(jedis.get(keyToTry));
          }
        }
        if (jedis.exists(timedUserKey)) {
          jedis.incr(timedUserKey);
          result += Integer.parseInt(jedis.get(timedUserKey));
        } else {
          jedis.set(timedUserKey, "1");
          jedis.expire(timedUserKey, COUNT_EXPIRY_PERIOD_SECONDS*2);
          result++;
        }
        logger.debug("Current count for user:" + timedUserKey + " - " + result);
        if (result > MAX_REQUESTS_PER_PERIOD){
          logger.debug("Reached user, flagging as reached limit");
          jedis.set(userKey, "" + ++result); // flag user
          jedis.expire(userKey, COUNT_EXPIRY_PERIOD_SECONDS*2); // throttle users harder who hit the cap
        }
      }
    } finally {
      if (jedis!=null) {
        jedis.close();
      }
    }
    return result;
  }
}