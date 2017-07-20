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

import java.util.Enumeration;

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

  public static final int PERIOD_MULTIPLIER = 10;
  public static final int MAX_REQUESTS_PER_PERIOD = COUNT_EXPIRY_PERIOD_SECONDS * PERIOD_MULTIPLIER;
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
      if (logger.isDebugEnabled()) {
        debugRequestHeaders(request);
      }
      String address = request.getHeader("requestx-forwarded-for");
      if (address == null || address.length() == 0 || "unknown".equalsIgnoreCase(address)) {
        address = request.getHeader("x-cluster-client-ip");
      }
      if (address == null || address.length() == 0 || "unknown".equalsIgnoreCase(address)) {
        address = request.getRemoteAddr();
      }
      if (address == null || address.length() == 0 || "unknown".equalsIgnoreCase(address)) {
        address = "127.0.0.1";
      }
      if (!address.equals("127.0.0.1") && !address.equals("0:0:0:0:0:0:0:1")) {
        try {
          logger.debug("About to increment count for user: " + address);
          int incrementUserGetCount = rateLimitService.incrementLimit("GET~" + address, jedisPool);
          logger.debug("Current count for user: " + address + " is: " + incrementUserGetCount);
          if (incrementUserGetCount >= MAX_REQUESTS_PER_PERIOD) { // temp ban user
            response.sendError(429, "Rate limit exceeded: " + MAX_REQUESTS_PER_PERIOD + " requests per " +
                COUNT_EXPIRY_PERIOD_SECONDS + " seconds. Please wait " + COUNT_EXPIRY_PERIOD_SECONDS * 2 + " seconds to try again.");
            result = false;
            logger.info("Throttled connections for user: " + address);
          } else {
            response.addIntHeader("Remaining request count", MAX_REQUESTS_PER_PERIOD - incrementUserGetCount);
          }
        } catch (Exception e) {
          logger.error("PROBLEM DEALING WITH RATE LIMITER: ", e);
        }
      }
    }
    return result;
  }

  /**
   * This method outputs the HTTP request headers to the debug logger.
   * @param request the HTTP request.
   */
  private void debugRequestHeaders(HttpServletRequest request) {
    logger.debug(request.getRemoteAddr() + ": Printing all headers...");
    logger.debug("Throttled connections for user: " + request.getRemoteAddr());
    Enumeration headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = (String) headerNames.nextElement();
      logger.debug(request.getRemoteAddr() + ": Header name: " + headerName);
      logger.debug(request.getRemoteAddr() + ":     Header value: " + request.getHeader(headerName));
    }
    logger.debug("Finished printing all headers!");
  }
}