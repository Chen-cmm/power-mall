package com.powernode.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.powernode.constant.BusinessEnum;
import com.powernode.constant.HttpConstants;
import com.powernode.constant.ResourceConstants;
import com.powernode.filter.TokenTranslationFilter;
import com.powernode.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.util.ConditionalOnBootstrapEnabled;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Spring Security安全框架的资源服务配置
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)//调用方法之前验证
public class ResourceServerConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private TokenTranslationFilter tokenTranslationFilter;

    //鉴权
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //关闭跨站请求伪造
        http.csrf().disable();
        //关闭跨域请求
        http.cors().disable();
        //关闭session策略
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        //编写一个token解析过滤器，将token转换为Security安全框架能够认证的用户信息,再存放到当前资源服务器容器中
        //要在UsernamePasswordAuthenticationFilter过滤器认证之前，把用户信息放到资源服务器容器中，这样就能直接认证通过
        //实际上资源服务器根本不需要认证，但是由于只要是依赖的Security框架的都默认要依赖一遍，所以才有这种方案
        http.addFilterBefore(tokenTranslationFilter, UsernamePasswordAuthenticationFilter.class);

        //配置处理携带token但权限不足的请求
        http.exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint())//处理没有携带token的请求
                .accessDeniedHandler(accessDeniedHandler());//处理携带token，但是权限不足的请求
        //配置其他请求
        http.authorizeHttpRequests()
                .antMatchers(ResourceConstants.RESOURCE_ALLOW_URLS)//这些直接放行，其他的都需要认证
                .permitAll()
                .anyRequest().authenticated();//除了需要放行的请求，都需要进行身份的认证
    }

    /**
     * 处理请求没有携带token
     * @return
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(){
        return (request, response, authException) -> {
            //设置响应头信息
            response.setContentType(HttpConstants.APPLICATION_JSON);
            response.setCharacterEncoding(HttpConstants.UTF_8);

            //创建项目统一响应结果对象
            Result<Object> result = Result.fail(BusinessEnum.UN_AUTHORIZATION);
            ObjectMapper objectMapper = new ObjectMapper();
            String s = objectMapper.writeValueAsString(result);
            PrintWriter writer = response.getWriter();
            writer.write(s);
            writer.flush();
            writer.close();
        };
    }

    /**
     * 处理携带了token但是权限不足
     * @return
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler(){
        return (request, response, accessDeniedException) -> {
            //设置响应头信息
            response.setContentType(HttpConstants.APPLICATION_JSON);
            response.setCharacterEncoding(HttpConstants.UTF_8);

            //创建项目统一响应结果对象
            Result<Object> result = Result.fail(BusinessEnum.ACCESS_DENY_FAIL);
            ObjectMapper objectMapper = new ObjectMapper();
            String s = objectMapper.writeValueAsString(result);
            PrintWriter writer = response.getWriter();
            writer.write(s);
            writer.flush();
            writer.close();
        };
    }
}
