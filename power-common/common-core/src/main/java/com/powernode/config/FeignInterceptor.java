package com.powernode.config;

import cn.hutool.core.util.ObjectUtil;
import com.powernode.constant.AuthConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * feign拦截器，在服务调用之间传递token
 */
public class FeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        //获取当前请求的上下文对象
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //判断是否有值
        if (ObjectUtil.isNotNull(requestAttributes)){
            //获取请求对象
            HttpServletRequest request = requestAttributes.getRequest();
            //判断request对象是否有值
            if (ObjectUtil.isNotNull(request)){
                //获取当前请求头中的token值，传递到下一个请求对象的请求头中
                String authorization = request.getHeader(AuthConstants.AUTHORIZATION);
                requestTemplate.header(AuthConstants.AUTHORIZATION,authorization);
                return;
            }
        }
    }
}
