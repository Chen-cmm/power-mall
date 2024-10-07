package com.powernode.util;


import com.powernode.model.SecurityUser;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

public class AuthUtils {
    /**
     * 获取用户对象
     * @return
     */
    public static SecurityUser getLoginUser(){
        return (SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /**\
     * 获取用户id
     * @return
     */
    public static Long getLoginUserId(){
        SecurityUser securityUser = (SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = securityUser.getUserId();
        return userId;
    }
    /**
     * 获取认证用户的操作权限集合
     */
    public static Set<String> getLoginUserPerms() {
        return getLoginUser().getPerms();
    }
}
