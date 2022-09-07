package com.itheima.reggie_take_out.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itheima.reggie_take_out.common.R;
import com.itheima.reggie_take_out.entity.Dish;
import com.itheima.reggie_take_out.entity.User;
import com.itheima.reggie_take_out.service.impl.UserImpl;
import com.itheima.reggie_take_out.utils.Message;
import com.itheima.reggie_take_out.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Denny
 * @create 2022-08-26 13:30
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController
{
    @Autowired
    private UserImpl userImpl;
    @Autowired
    private RedisTemplate redisTemplate;
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session)
    {

        log.info(user.getPhone());

        Integer ValidateCode = ValidateCodeUtils.generateValidateCode(4);
        /*session.setAttribute(user.getPhone(),ValidateCode);//在session中建立phone和vcode映射,防止同一时间不同手机登录验证码的混淆*/
        redisTemplate.opsForValue().set(user.getPhone(),ValidateCode,1, TimeUnit.MINUTES);//这里用redis替换session，并设置过期时间
        log.info(ValidateCode.toString());
        try {
            Message.run(user.getPhone(),ValidateCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return R.success("发送验证码成功");
    }
    @PostMapping("/login")
    public R<User> login( @RequestBody Map map,HttpSession httpSession)
    {
        //先查是否有这个手机号
        String phone = map.get("phone").toString();
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<User>();
        userQueryWrapper.eq("phone",phone);//phone不可以重复的
        User DBuser = userImpl.getOne(userQueryWrapper);
        if(DBuser==null)
        {
            return R.error("不存在该用户");
        }
        //验证码手机号是否匹配
        String code = map.get("code").toString();

        /*String Sessioncode = httpSession.getAttribute(phone).toString();*/
        String Sessioncode=redisTemplate.opsForValue().get(phone).toString();//redis替换
        if(Sessioncode.equals(code))
        {
            httpSession.setAttribute("user",DBuser.getId());
            redisTemplate.delete(phone);//登录成功还要删除redis数据对象
            return R.success(DBuser);

        }

        else return R.error("登录失败");




    }
}
