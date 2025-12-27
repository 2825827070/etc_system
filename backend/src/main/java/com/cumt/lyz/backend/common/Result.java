/**
 * 通用响应结果类
 * 用于统一封装API接口的返回数据格式
 * 包含状态码、消息提示和具体数据
 *
 * @param <T> 泛型参数，表示返回数据的具体类型
 */
package com.cumt.lyz.backend.common;

import lombok.Data;

/**
 * 响应结果实体类
 * 使用Lombok的@Data注解自动生成getter、setter、toString等方法
 *
 * @param <T> 响应数据的类型
 */
@Data
public class Result<T> {
    /**
     * 响应状态码
     * 200表示成功，500表示失败
     */
    private Integer code;

    /**
     * 响应消息
     * 用于描述操作结果的信息
     */
    private String message;

    /**
     * 响应数据
     * 具体返回的业务数据
     */
    private T data;

    /**
     * 创建成功响应结果（带数据）
     *
     * @param <T> 数据类型
     * @param data 响应数据
     * @return 封装好的成功响应结果
     */
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage("success");
        r.setData(data);
        return r;
    }

    /**
     * 创建成功响应结果（带自定义消息和数据）
     *
     * @param <T> 数据类型
     * @param msg 自定义成功消息
     * @param data 响应数据
     * @return 封装好的成功响应结果
     */
    public static <T> Result<T> success(String msg, T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage(msg);
        r.setData(data);
        return r;
    }

    /**
     * 创建失败响应结果
     *
     * @param msg 错误消息
     * @return 封装好的失败响应结果
     */
    public static Result<Void> fail(String msg) {
        Result<Void> r = new Result<>();
        r.setCode(500);
        r.setMessage(msg);
        return r;
    }
}