package com.powernode.model;

import com.powernode.constant.BusinessEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 项目同一响应结果对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("项目同一响应结果对象")
public class Result<T> implements Serializable {//对象需要在网络中传输，所以就要求对象可以序列化
    @ApiModelProperty("状态码")
    private Integer code = 200;
    @ApiModelProperty("消息")
    private String msg = "ok";
    @ApiModelProperty("数据")
    private T data;

    public static <T> Result<T> success(T data){
        Result result = new Result();
        result.setData(data);
        return result;
    }

    public static <T> Result<T> fail(Integer code,String msg){
        Result result = new Result();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    public static <T> Result<T> fail(BusinessEnum businessEnum){
        Result result = new Result();
        result.setCode(businessEnum.getCode());
        result.setMsg(businessEnum.getMsg());
        return result;
    }
}
