package com.itheima.reggie_take_out.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie_take_out.DTO.DishDto;
import com.itheima.reggie_take_out.DTO.OrdersDto;
import com.itheima.reggie_take_out.common.BaseContext;
import com.itheima.reggie_take_out.common.CustomException;
import com.itheima.reggie_take_out.common.R;
import com.itheima.reggie_take_out.entity.*;
import com.itheima.reggie_take_out.mapper.OrdersMapper;
import com.itheima.reggie_take_out.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private OrderDetailServiceImpl orderDetailServiceImpl;





    public Page QueryPage(int page, int pageSize, String number, String beginTime,String endTime)
    {
        QueryWrapper<Orders> wrapper=new QueryWrapper<>();
        wrapper.orderByDesc("id");
        if(number!=null)
        {
            wrapper.eq("number",number);
        }
        if(beginTime!=null&&endTime!=null)
        {
            wrapper.ge("order_time",beginTime);
            wrapper.le("order_time",endTime);
        }


        Page<Orders> ordersPage = new Page<>(page, pageSize);
        return this.page(ordersPage,wrapper);
    }

    @Override
    public void ChangeStatus(Orders orders) {
        this.updateById(orders);
    }

    @Override
    public Page<OrdersDto> userOrders(int page, int pageSize, String userId)
    {
        //Page的结构：Page->名为records的list的属性->list存放多个OrdersDto
        //OrdersDto的结构：用户名、地址、电话和list-》list存放多个orderdetail
        //一个orderdetail=一个商品，一个orderdto=一个order+多个orderdetail

        //1.
        Page<Orders> ordersPage=new Page<>();
        QueryWrapper<Orders> wrapper=new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        this.page(ordersPage,wrapper);

        //2.
        Page<OrdersDto> ordersDtoPage=new Page<>();
        BeanUtils.copyProperties(ordersPage,ordersDtoPage,"records");
        ArrayList<OrdersDto> list1=new ArrayList<>();

        //3.
        for (Orders orders:ordersPage.getRecords()
             )
        {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(orders,ordersDto);
            //订单号
            String ordersNumber = orders.getNumber();
            QueryWrapper<OrderDetail> wrapper1=new QueryWrapper<>();
            wrapper.eq("order_id",ordersNumber);
            List<OrderDetail> list = orderDetailServiceImpl.list(wrapper1);
            ordersDto.setOrderDetails(list);
            list1.add(ordersDto);



        }
        ordersDtoPage.setRecords(list1);
        return ordersDtoPage;





    }


    /**
     * 用户下单
     * @param orders
     * 应该不是我写的
     */
    @Transactional
    public void submit(Orders orders) {
        //获得当前用户id
        Long userId = BaseContext.getCurrentId();

        //查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(wrapper);

        if(shoppingCarts == null || shoppingCarts.size() == 0){
            throw new CustomException("购物车为空，不能下单");
        }

        //查询用户数据
        User user = userService.getById(userId);

        //查询地址数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if(addressBook == null){
            throw new CustomException("用户地址信息有误，不能下单");
        }

        long orderId = IdWorker.getId();//订单号

        AtomicInteger amount = new AtomicInteger(0);

        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());


        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        //向订单表插入数据，一条数据
        this.save(orders);

        //向订单明细表插入数据，多条数据
        orderDetailService.saveBatch(orderDetails);

        //清空购物车数据
        shoppingCartService.remove(wrapper);
    }
}