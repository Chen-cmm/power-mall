package com.powernode.model;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录同一结果对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("登录统一结果对象")
public class LoginResult {
    @ApiModelProperty("令牌token")
    private String accessToken;
    @ApiModelProperty("有效时长")
    private Long expireIn;
}
