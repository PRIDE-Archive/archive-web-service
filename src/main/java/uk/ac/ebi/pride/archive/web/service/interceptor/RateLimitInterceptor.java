package uk.ac.ebi.pride.archive.web.service.interceptor;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

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

  public static final int PERIOD_MULTIPLIER = 2;
  public static final int MAX_REQUESTS_PER_PERIOD = COUNT_EXPIRY_PERIOD_SECONDS * PERIOD_MULTIPLIER;
  @Value("#{redisConfig['redis.host']}")
  private String redisServer;
  @Value("#{redisConfig['redis.port']}")
  private String redisPort;
  private JedisCluster jedisCluster;


  @Autowired
  private RateLimitService rateLimitService;

  /**
   * This method is called before handling every single request.
   * @param request the request sent to the Web Service.
   * @param response the response sent back to the user.
   * @param handler the handler object.
   * @return true to process the request onwards as normal, false otherwise and the request is not processed at all.
   */
  public boolean preHandle(HttpServletRequest request,
                           HttpServletResponse response, Object handler) {
    boolean result = true;
    setupRedisConnection();
    if ("GET".equalsIgnoreCase(request.getMethod())) {
      if (logger.isDebugEnabled()) {
        debugRequestHeaders(request);
      }
      String address = request.getHeader("requestx-forwarded-for");
      final String UNKNOWN = "unknown";
      final String LOCALHOST = "127.0.0.1";
      final String ALT_LOCALHOST = "0:0:0:0:0:0:0:1";
      if (StringUtils.isEmpty(address) || UNKNOWN.equalsIgnoreCase(address)) {
        address = request.getHeader("x-cluster-client-ip");
      }
      if (StringUtils.isEmpty(address) || UNKNOWN.equalsIgnoreCase(address)) {
        address = request.getRemoteAddr();
      }
      if (StringUtils.isEmpty(address) || UNKNOWN.equalsIgnoreCase(address)) {
        address = LOCALHOST;
      }
      if (!LOCALHOST.equals(address) && !ALT_LOCALHOST.equals(address)) {
        try {
          logger.debug("About to increment count for user: " + address);
          int incrementUserGetCount = rateLimitService.incrementLimit("GET~" + address, jedisCluster);
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
   * Sets up the connection to Redis cluster.
   */
  private void setupRedisConnection() {
    if (jedisCluster == null) {
      final String STRING_SEPARATOR = "##";
      Set<HostAndPort> jedisClusterNodes = new HashSet<>();
      if (redisServer.contains(STRING_SEPARATOR)) {
        String[] servers = redisServer.split(STRING_SEPARATOR);
        String[] ports;
        if (redisPort.contains(STRING_SEPARATOR)) {
          ports = redisPort.split(STRING_SEPARATOR);
        } else {
          ports = new String[]{redisPort};
        }
        if (ports.length!=1 && ports.length!=servers.length) {
          logger.error("Mismatch between provided Redis ports and servers. Should either have 1 port for all servers, or 1 port per server");
        }
        for (int i=0; i<servers.length; i++) {
          String serverPort = ports.length == 1 ? ports[0] : ports[i];
          jedisClusterNodes.add(new HostAndPort(servers[i], Integer.parseInt(serverPort)));
          logger.info("Added Jedis node: " + servers[i] + " " + serverPort);
        }
      } else {
        jedisClusterNodes.add(new HostAndPort(redisServer, Integer.parseInt(redisPort))); //Jedis Cluster will attempt to discover cluster nodes automatically
        logger.info("Added Jedis node: " + redisServer + " " + redisPort);
      }
      jedisCluster =  new JedisCluster(jedisClusterNodes, new JedisPoolConfig());
    }
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
      logger.debug(request.getRemoteAddr() + ": Header value: " + request.getHeader(headerName));
    }
    logger.debug("Finished printing all headers!");
  }
}