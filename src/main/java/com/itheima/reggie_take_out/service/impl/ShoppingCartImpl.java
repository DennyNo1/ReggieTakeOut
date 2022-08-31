package com.itheima.reggie_take_out.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie_take_out.entity.ShoppingCart;
import com.itheima.reggie_take_out.mapper.ShoppingCartMapper;
import com.itheima.reggie_take_out.service.ShoppingCartService;
import org.springframework.stereotype.Service;

/**
 * @author Denny
 * @create 2022-08-30 14:33
 */
@Service
public class ShoppingCartImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
