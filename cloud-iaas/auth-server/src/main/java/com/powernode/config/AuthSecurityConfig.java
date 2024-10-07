package com.powernode.config;

import ch.qos.logback.classic.sift.AppenderFactoryUsingJoran;
import cn.hutool.json.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powernode.constant.AuthConstants;
import com.powernode.constant.BusinessEnum;
import com.powernode.constant.HttpConstants;
import com.powernode.impl.UserDetailServiceImpl;
import com.powernode.model.LoginResult;
import com.powernode.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.util.ConditionalOnBootstrapEnabled;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.UUID;

/**
 * Security安全框架配置类
 * 授权
 */
@Configuration
public class AuthSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailServiceImpl userDetailService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 设置security安全框架  走自己的认证流程
     * @param auth
     * @throws Exception
     * 认证
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailService);
    }

    //授权
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //关闭跨站请求伪造
        http.cors().disable();
        //关闭跨域请求
        http.csrf().disable();
        //关闭session使用策略
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        //配置登录信息
        http.formLogin()
                .loginProcessingUrl(AuthConstants.LOGIN_URL)//设置登录的url
                .successHandler(authenticationSuccessHandler()) //设置登陆成功处理器
                .failureHandler(authenticationFailureHandler());//设置登陆失败处理器
        //配置登出信息
        http.logout()
                .logoutUrl(AuthConstants.LOGOUT_URL)//设置登出url
                .logoutSuccessHandler(logoutSuccessHandler());//设置登出成功处理器
        //要求所有请求都需要进行身份的认证
        http.authorizeHttpRequests().anyRequest().authenticated();
    }

    /**
     * 登陆成功处理器
     * @return
     */
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler(){
        return (request, response, authentication) -> {
            //登录成功之后把token响应给前端
            //设置响应头信息
            response.setContentType(HttpConstants.CONTENT_TYPE);
            response.setCharacterEncoding(HttpConstants.UTF_8);

            //使用UUID来当作token
            String token = UUID.randomUUID().toString();
            //从security框架中获取认证用户对象(自己封装的SecurityUser)并转换为json字符串格式
            String userJsonStr = JSONObject.toJSONString(authentication.getPrincipal());
            //将token当作key，认证用户对象的json格式的字符串当作value存放到redis中
            stringRedisTemplate.opsForValue().set(AuthConstants.LOGIN_TOKEN_PREFIX+token,userJsonStr, Duration.ofSeconds(AuthConstants.TOKEN_TIME));

            //封装登录统一结果对象
            LoginResult loginResult = new LoginResult(token,AuthConstants.TOKEN_TIME);
            //创建响应结果对象
            Result<Object> result = Result.success(loginResult);

            //返回结果
            ObjectMapper objectMapper = new ObjectMapper();
            String s = objectMapper.writeValueAsString(result);
            PrintWriter writer = response.getWriter();
            writer.write(s);
            writer.flush();
            writer.close();
        };
    }

    /**
     * 登陆失败处理器
     * @return
     */
    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler(){
        return (request, response, exception) -> {
            response.setContentType(HttpConstants.APPLICATION_JSON);
            response.setCharacterEncoding(HttpConstants.UTF_8);

            //创建同一响应结果对象
            Result<Object> result = new Result<>();
            result.setCode(BusinessEnum.OPERATION_FAIL.getCode());
            if (exception instanceof BadCredentialsException){
                result.setMsg("用户名或密码有误");
            }else if(exception instanceof UsernameNotFoundException){
                result.setMsg("用户不存在");
            } else if (exception instanceof AccountExpiredException) {
                result.setMsg("账号异常，请联系管理员");
            } else if (exception instanceof AccountStatusException) {
                result.setMsg("账号异常，请联系管理员");
            } else if (exception instanceof InternalAuthenticationServiceException) {
                result.setMsg(exception.getMessage());
            }else {
                result.setMsg("操作异常");
            }

            //返回结果
            ObjectMapper objectMapper = new ObjectMapper();
            String s = objectMapper.writeValueAsString(result);
            PrintWriter writer = response.getWriter();
            writer.write(s);
            writer.flush();
            writer.close();
        };
    }
    /**
     * 登出成功处理器
     */
    @Bean
    public LogoutSuccessHandler logoutSuccessHandler(){
        return (request, response, authentication) -> {
            //设置响应头信息
            response.setContentType(HttpConstants.APPLICATION_JSON);
            response.setCharacterEncoding(HttpConstants.UTF_8);

            //从请求头中获取token
            String authorization = request.getHeader(AuthConstants.AUTHORIZATION);
            String token = authorization.replaceFirst(AuthConstants.BEARER, "");
            //将当前token从redis中删除
            stringRedisTemplate.delete(AuthConstants.LOGIN_TOKEN_PREFIX+token);
            //创建统一响应结果
            Result<Object> result = Result.success(null);
            //返回结果
            ObjectMapper objectMapper = new ObjectMapper();
            String s = objectMapper.writeValueAsString(result);
            PrintWriter writer = response.getWriter();
            writer.write(s);
            writer.flush();
            writer.close();
        };
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}
