package com.powernode.impl;

import cn.hutool.core.util.StrUtil;
import com.powernode.constant.AuthConstants;
import com.powernode.factory.LoginStrategyFactory;
import com.powernode.strategy.LoginStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 项目自己的认证流程
 */
@Service
public class UserDetailServiceImpl implements UserDetailsService {
    @Autowired
    private LoginStrategyFactory loginStrategyFactory;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //获取请求对象
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        //从请求头中获取登录类型
        String loginType = request.getHeader(AuthConstants.LOGIN_TYPE);
        if (StrUtil.isBlank(loginType)){
            throw new InternalAuthenticationServiceException("非法登录，登录类型不匹配");
        }
        //判断请求来自于哪个系统
        LoginStrategy loginStrategy = loginStrategyFactory.getInstance(loginType);
        return loginStrategy.realLogin(username);
    }
}
