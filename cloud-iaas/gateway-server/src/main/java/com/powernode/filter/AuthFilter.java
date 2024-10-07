package com.powernode.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powernode.config.WhiteUrlsConfig;
import com.powernode.constant.AuthConstants;
import com.powernode.constant.BusinessEnum;
import com.powernode.constant.HttpConstants;
import com.powernode.model.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Date;

/**全局token过滤器
 * 前后端约定好令牌存放的位置：请求头的Authorization 中，值为bearer + “ ” + token
 */
@Component
@Slf4j
public class AuthFilter implements GlobalFilter, Ordered {

    @Autowired
    private WhiteUrlsConfig whiteUrlsConfig;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 校验token
     * 1.获取请求路径
     * 2.判断请求路径是否可以直接放行
     *   可以：不需要验证
     *   不可以：需要进行身份认证
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取请求对象
        ServerHttpRequest request = exchange.getRequest();
        //获取请求路径
        String path = request.getPath().toString();
        //判断当前请求路径是否直接放行,路径是否存在于白名单中
        //TODO 改为用match，这样判断不准确
        if (whiteUrlsConfig.getAllowUrls().contains(path)){
            //可以直接放行
            return chain.filter(exchange);
        }
        //需要验证token
        //从请求头中获取token
        String authorization = request.getHeaders().getFirst(AuthConstants.AUTHORIZATION);
        //判断是否有值
        if (!StringUtils.hasText(authorization)){
            log.error("拦截非法请求,时间：{},请求API路径",new Date(),path);
            /*ServerHttpResponse response = exchange.getResponse();
            //设置响应头信息
            response.getHeaders().set(HttpConstants.CONTENT_TYPE,HttpConstants.APPLICATION_JSON);
            //设置响应消息  TODO 这里也太麻烦了  看看其他项目怎么写的  不太对劲
            Result<Object> result = Result.fail(BusinessEnum.UN_AUTHORIZATION);
            ObjectMapper objectMapper = new ObjectMapper();
            byte[] bytes = new byte[0];
            try {
                bytes = objectMapper.writeValueAsBytes(result);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            DataBuffer dataBuffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(dataBuffer));*/
            ServerHttpResponse response = exchange.getResponse();
            response.setRawStatusCode(401);
            //Complete代表完结了
            return response.setComplete();
        }
        //从Authorization中获取token
        String tokenValue = authorization.replace(AuthConstants.BEARER, "");
        //判断token值是否有值
        if (!StringUtils.hasText(tokenValue) || !stringRedisTemplate.hasKey(AuthConstants.LOGIN_TOKEN_PREFIX+tokenValue)){
            log.error("拦截非法请求,时间：{},请求API路径",new Date(),path);
            /*ServerHttpResponse response = exchange.getResponse();
            //设置响应头信息
            response.getHeaders().set(HttpConstants.CONTENT_TYPE,HttpConstants.APPLICATION_JSON);
            //设置响应消息  TODO 这里也太麻烦了  看看其他项目怎么写的  不太对劲
            Result<Object> result = Result.fail(BusinessEnum.UN_AUTHORIZATION);
            ObjectMapper objectMapper = new ObjectMapper();
            byte[] bytes = new byte[0];
            try {
                bytes = objectMapper.writeValueAsBytes(result);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            DataBuffer dataBuffer = response.bufferFactory().wrap(bytes);*/
            ServerHttpResponse response = exchange.getResponse();
            response.setRawStatusCode(401);
            //Complete代表完结了
            return response.setComplete();
        }
        //token不仅有值，而且在redis中存在，说明身份认证通过
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -5;
    }
}
