package com.itheima.reggie_take_out.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie_take_out.entity.User;
import com.itheima.reggie_take_out.mapper.UserMapper;
import com.itheima.reggie_take_out.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @author Denny
 * @create 2022-08-26 13:28
 */
@Service
public class UserImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
