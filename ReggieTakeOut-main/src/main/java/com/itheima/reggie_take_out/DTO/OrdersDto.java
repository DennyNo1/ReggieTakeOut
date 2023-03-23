package com.itheima.reggie_take_out.DTO;


import com.itheima.reggie_take_out.entity.OrderDetail;
import com.itheima.reggie_take_out.entity.Orders;
import lombok.Data;
import java.util.List;

@Data
public class OrdersDto extends Orders {

    //这个dto设计得有问题，前四个属性orders已拥有。
    //用于历史订单
    private String userName;

    private String phone;

    private String address;

    private String consignee;

    private List<OrderDetail> orderDetails;
	
}
