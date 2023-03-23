package com.itheima.reggie_take_out.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie_take_out.common.BaseContext;
import com.itheima.reggie_take_out.common.R;
import com.itheima.reggie_take_out.entity.Orders;
import com.itheima.reggie_take_out.service.OrdersService;
import com.itheima.reggie_take_out.service.impl.OrdersServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

/**
 * 订单
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrdersService orderService;
    @Autowired
    private OrdersServiceImpl orderServiceImpl;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据：{}",orders);
        orderService.submit(orders);
        return R.success("下单成功");
    }
    /**
     * 商家对订单有条件或者全部的查询
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String number, String beginTime,String endTime)

    {
        log.info("{},{},{},{},{}",page,pageSize,number,beginTime,endTime);
        return R.success(orderServiceImpl.QueryPage(page,pageSize,number,beginTime,endTime));//按照前端设计，往R.success里返回一个Page对象即可

    }
    /**
     * 商家修改订单状态
     * 订单状态 1待付款，2待派送，3已派送，4已完成，5已取消
     * 2-3，3-4
     */
    @PutMapping
    public R<String> ChangeStatus(@RequestBody Orders orders){
        log.info("订单数据：{}",orders);
        orderService.ChangeStatus(orders);
        return R.success("状态更改完毕");
    }
    /**
     * 查询个人订单
     */
    @GetMapping("/userPage")
    public R<Page> userOrders(int page,int pageSize)//,@RequestBody HttpSession httpSession)
    {
        //这里Page里面包含的是OrdersDto
        //首先获取用户id
        //因为getCurrentId()是静态方法，所以可以直接写类名。
        //为什么可以这么写？filter优先于这个方法收到前端请求，且在过滤的时候，会把用户id存入BaseContext。
        String userId= BaseContext.getCurrentId().toString();
        //String userId="1";
        return R.success(orderServiceImpl.userOrders(page,pageSize,userId));

    }

}