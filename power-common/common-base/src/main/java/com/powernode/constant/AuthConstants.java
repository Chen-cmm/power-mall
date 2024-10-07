package com.powernode.constant;

import cn.hutool.core.lang.copier.SrcToDestCopier;

/**
 * 认证授权常量类
 */
public interface AuthConstants {
    /**
     * token值的key
     */
    String AUTHORIZATION = "Authorization";
    /**
     * token值的前缀
     */
    String BEARER = "bearer ";
    /**
     * token值存放在redis中的前缀
     */
    String LOGIN_TOKEN_PREFIX = "login_token:";
    /**
     * 登录的URL
     */
    String LOGIN_URL = "/doLogin";
    /**
     * 登出的URL
     */
    String LOGOUT_URL = "/doLogout";

    /**
     * 登陆类型
     */
    String LOGIN_TYPE = "loginType";

    /**
     * 登录用户来自后台管理系统的标识
     */
    String SYS_USER_LOGIN = "sysUserLogin";

    /**
     * 登录用户来自用户购物系统的标识
     */
    String MEMBER_LOGIN = "memberLogin";
    /**
     * token的有效期
     */
    Long TOKEN_TIME = 14400L;
}
