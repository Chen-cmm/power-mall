package com.powernode.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "swagger3")
public class SwaggerProperties {
    /**
     * 扫描生成文档的包名
     */
    private String basePackage;
    /**
     * 作者名称
     */
    private String name;
    /**
     * 主页地址
     */
    private String url;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 标题
     */
    private String title;
    /**
     * 描述
     */
    private String description;
    /**
     * 服务信息
     */
    private String license;
    /**
     * 服务信息URL地址
     */
    private String licenseUrl;
    /**
     * 服务团队
     */
    private String termsOfServiceUrl;
    /**
     * 版本号
     */
    private String version;
}
