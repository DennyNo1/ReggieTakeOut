package com.itheima.reggie_take_out.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import com.itheima.reggie_take_out.DTO.OrdersDto;
import com.itheima.reggie_take_out.entity.Orders;

import java.time.LocalDateTime;

public interface OrdersService extends IService<Orders> {

    /**
     * 用户下单
     * @param orders
     */
    public void submit(Orders orders);

    public Page<Orders> QueryPage(int page, int pageSize, String number, String beginTime, String endTime);

    public void ChangeStatus(Orders orders);
    public Page<OrdersDto> userOrders(int page, int pageSize,String userId);
}
