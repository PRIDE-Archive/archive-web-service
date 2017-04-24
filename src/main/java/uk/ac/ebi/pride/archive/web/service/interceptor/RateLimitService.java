package uk.ac.ebi.pride.archive.web.service.interceptor;

import redis.clients.jedis.JedisPool;

/** This defines the interface for limiing the rate of users' requests.
 *
 * @author Tobias Ternent
 */
public interface RateLimitService {

  public int incrementLimit(String userKey, JedisPool jedisPool) throws Exception;
}
