package com.itheima.reggie_take_out.DTO;


import com.itheima.reggie_take_out.entity.Dish;
import com.itheima.reggie_take_out.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();//List
    /*
    这2个以后再用
     */
    private String categoryName;

    private Integer copies;


}
