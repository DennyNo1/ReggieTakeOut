package com.itheima.reggie_take_out.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie_take_out.common.BaseContext;
import com.itheima.reggie_take_out.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.PathMatcher;

/**
 * @author Denny
 * @create 2022-08-16 17:20
 */
@Slf4j
@Configuration
@WebFilter("/*")
public class LoginCheckFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException
    {
        HttpServletRequest request=(HttpServletRequest)servletRequest;
        //HttpServletRequest继承ServletRequest，可认为是C->S端的消息
        HttpServletResponse response=(HttpServletResponse) servletResponse;
        String urls[]=
                {       "/user/sendMsg",
                        "/employee/login",
                        "/user/login",
                        "/employee/logout",
                        "/backend/**",
                        "/front/**"//前后端的静态资源被认为是游客可以访问的

                };
        String requestUrl=request.getRequestURI();//从消息中获取请求路径
        /**
         * 是否匹配游客可访问的路径
         */
        if(match(urls,requestUrl))
        {
            log.info("本次请求{}不需要拦截",requestUrl);
            filterChain.doFilter(request,response);
            return;//不加return会继续运行下去
        }

        /**
         * 是否employee已登录
         */
        if(request.getSession().getAttribute("employee")!=null)
        {
            log.info("employee已登录，用户id为：{}",request.getSession().getAttribute("employee"));
            BaseContext.setId((Long) request.getSession().getAttribute("employee"));//要写在dofilter之前，否则就放行过去，取不到值了
            filterChain.doFilter(request,response);

            return;
        }
        /**
         * 是否user已登录
         */
        if(request.getSession().getAttribute("user")!=null)
        {
            log.info("user已登录，用户id为：{}",request.getSession().getAttribute("user"));
            BaseContext.setId((Long) request.getSession().getAttribute("user"));//为了给这个线程设置唯一id
            filterChain.doFilter(request,response);

            return;
        }

        /**
         * 返回一个与前端沟通好的消息
         */
        log.info("用户未登录,本次请求是{}",requestUrl);
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));



    }
    public boolean match(String urls[],String requestUrl)
    {
        AntPathMatcher matcher=new AntPathMatcher();
        //因为前后端静态资源的路径无法直接和requestUrl进行匹配，所以需要这个我没见过的匹配器
        for ( String s:urls
             )
        {
             if(matcher.match(s,requestUrl))
                 return true;

        }
        return false;

    }
}
