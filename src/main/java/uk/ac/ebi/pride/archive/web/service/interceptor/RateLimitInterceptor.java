package uk.ac.ebi.pride.archive.web.service.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static uk.ac.ebi.pride.archive.web.service.interceptor.RateLimitServiceImpl.COUNT_EXPIRY_PERIOD_SECONDS;

/**
 * This class rate limits all Web Service GET requests. This is according to the values set for
 * MAX_REQUESTS_PER_PERIOD which is double the value set for RateLimitServiceImpl.COUNT_EXPIRY_PERIOD_SECONDS.
 * The purpose is to limit the frequency individual users may query for information, primarily in relation
 * to PSMs. Pagination alone does not solve such a problem.
 *
 * @author Tobias Ternent
 */
@Service
public class RateLimitInterceptor extends HandlerInterceptorAdapter {
  private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

  private static final int MAX_REQUESTS_PER_PERIOD = COUNT_EXPIRY_PERIOD_SECONDS * 2;
  @Value("#{redisConfig['redis.host']}")
  private String redisServer;
  @Value("#{redisConfig['redis.port']}")
  private String redisPort;
  @Value("#{redisConfig['redis.password']}")
  private String redisPassword;
  private JedisPool jedisPool;

  @Autowired
  private RateLimitService rateLimitService;

  /**
   * This method is called before handling every single request.
   * @param request the request sent to the Web Service.
   * @param response the response sent back to the user.
   * @param handler the handler object.
   * @return true to process the request onwards as normal, false otherwise and the request is not processed at all.
   * @throws Exception Any exception encountered attempting to limit the user's requests.
   */
  public boolean preHandle(HttpServletRequest request,
                           HttpServletResponse response, Object handler) throws Exception {
    boolean result = true;
    if (jedisPool==null) {
      jedisPool = new JedisPool(new JedisPoolConfig(), redisServer, Integer.parseInt(redisPort), 0, redisPassword);
    }
    if ("GET".equalsIgnoreCase(request.getMethod())) {
      int incrementUserGetCount = rateLimitService.incrementLimit("GET~" + request.getRemoteAddr(), jedisPool);
      logger.debug("Current count for user: " + request.getRemoteAddr() + " is: " + incrementUserGetCount);
      if (incrementUserGetCount >= MAX_REQUESTS_PER_PERIOD) {
        response.sendError(429, "Rate limit exceeded (" + MAX_REQUESTS_PER_PERIOD + "), please wait " + COUNT_EXPIRY_PERIOD_SECONDS + " seconds.");
        result = false;
        logger.info("Throttled connections for user: " + request.getRemoteAddr());
      } else {
        response.addIntHeader("Remaining request count", MAX_REQUESTS_PER_PERIOD - incrementUserGetCount);
      }
    }
    return result;
  }
}