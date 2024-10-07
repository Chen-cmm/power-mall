package com.powernode.filter;

import com.alibaba.fastjson.JSONObject;
import com.powernode.constant.AuthConstants;
import com.powernode.model.SecurityUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * token转换过滤器
 */
@Component
public class TokenTranslationFilter extends OncePerRequestFilter {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * token转换过滤器
     *  前提：
     *  只负责处理携带token的请求,然后将认证的用户信息转换出来
     *  没有携带token的请求，交给Security配置类中的处理器进行处理
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //1.获取token
        String authorizationValue = request.getHeader(AuthConstants.AUTHORIZATION);
        if (StringUtils.hasText(authorizationValue)){
            String token = authorizationValue.replaceFirst(AuthConstants.BEARER, "");
            //2.判断token是否有值
            if (StringUtils.hasText(token)){
                //走到这说明是请求资源服务器，并且携带了token，所以可以对用户的token续签
                Long expire = stringRedisTemplate.getExpire(AuthConstants.LOGIN_TOKEN_PREFIX + token);
                if (expire < AuthConstants.TOKEN_EXPIRE_THRESHOLD_TIME){
                    //存活时间小于一个小时，进行token续签
                    stringRedisTemplate.expire(AuthConstants.LOGIN_TOKEN_PREFIX + token,AuthConstants.TOKEN_TIME, TimeUnit.SECONDS);
                }


                //      有：token转换为用户信息，并将用户信息转换为security框架认识的用户信息对象，再将认识的用户信息对象存放到当前资源服务器容器
                String JsonStr = stringRedisTemplate.opsForValue().get(AuthConstants.LOGIN_TOKEN_PREFIX + token);
                //将Json格式字符串的认证用户信息转换为认证用户对象
                SecurityUser securityUser = JSONObject.parseObject(JsonStr, SecurityUser.class);
                //处理权限
                Set<SimpleGrantedAuthority> collect = securityUser.getPerms().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
                //创建UsernamePasswordAuthenticationToken
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(securityUser,null,collect);
                //将认证用户对象存放到当前模块的容器中
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        filterChain.doFilter(request,response);
    }
}
