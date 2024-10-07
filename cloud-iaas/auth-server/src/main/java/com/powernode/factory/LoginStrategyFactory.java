package com.powernode.factory;

import com.powernode.strategy.LoginStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录策略工厂类
 */
@Component
public class LoginStrategyFactory {
    /**
     * @Autowired 标注作用于 Map 类型时，如果 Map 的 key 为 String 类型，
     * 则 Spring 会将容器中所有类型符合 Map 的 value 对应的类型的 Bean 增加进来，用 Bean 的 id 或 name 作为 Map 的 key。
     */
    @Autowired
    private Map<String,LoginStrategy> loginStrategyMap = new HashMap<>();

    /**
     * 根据用户登录类型获取具体的登录策略
     * @param name
     * @return
     */
    public LoginStrategy getInstance(String name){
        return loginStrategyMap.get(name);
    }
}
