package com.itheima.reggie_take_out.common;

/**
 * 基于ThreadLocal封装工具类
 * @author Denny
 * @create 2022-08-19 13:42
 */
public class BaseContext
{
    private static ThreadLocal<Long> threadLocal=new ThreadLocal<>();
    public static void setId(Long id)
    {
        threadLocal.set(id);//只能存一个变量？
    }
    public static Long get()
    {
        return threadLocal.get();
    }

    public static Long getCurrentId() {
        return threadLocal.get();
    }
}
