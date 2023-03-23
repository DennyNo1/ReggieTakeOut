package com.itheima.reggie_take_out.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie_take_out.DTO.DishDto;
import com.itheima.reggie_take_out.common.R;
import com.itheima.reggie_take_out.entity.Category;
import com.itheima.reggie_take_out.entity.Dish;
import com.itheima.reggie_take_out.entity.DishFlavor;
import com.itheima.reggie_take_out.mapper.DishMapper;
import com.itheima.reggie_take_out.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Denny
 * @create 2022-08-19 15:35
 */
@Slf4j
@Service
public class DishImpl extends ServiceImpl<DishMapper,Dish> implements DishService
{
    @Autowired
    private CategoryImpl categoryImpl;
    @Autowired
    private DishFlavorImpl dishFlavorImpl;
    public boolean selectOneByIds(Long ids) {
        QueryWrapper<Dish> wrapper = new QueryWrapper<>();
        wrapper.eq("category_id", ids);

        if (this.count(wrapper)==0)//用count更便捷
        {
            return true;
        }

        else return false;


    }

    /**
     * 多表查询的分页
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    public Page<DishDto> showAllDish(int page, int pageSize,String name)
    {
        /*
        Page的主体就是List，加一些其他属性。
        由于this.page（wrapper,page），操作在形参page上。所以先造出Page<Dish>
         */
        //1.
        Page<Dish>dishPage = new Page<>(page, pageSize);
        QueryWrapper<Dish> wrapper=new QueryWrapper<>();
        wrapper.eq("is_deleted",0);
        if(name!=null)
        {
            wrapper.like("name",name);
        }
        this.page(dishPage,wrapper);
        //2.把Page<Dish>除List之外，全部复制给给Page<DishDto>

        Page<DishDto> dishdtoPage=new Page<>(page,pageSize);
        BeanUtils.copyProperties(dishPage,dishdtoPage,"records");
        List<DishDto> dishDtoList=new ArrayList<>();//这里必须把List填满，在把List放到Record。不可以直接对Records设置，大概是因为private。
        //3.把Page<Dish>的List的category_id拿出来查询，结果放到Page<DishDto>的List的categoryName。dish本身也要放进dishDto
        for (Dish dish:dishPage.getRecords()
             )
        {
            DishDto dishDto = new DishDto();//建立一个完整DishDto
            BeanUtils.copyProperties(dish,dishDto);//再一次复制
            /*
            查询
             */
            Long categoryId = dish.getCategoryId();
            Category category = categoryImpl.getById(categoryId);
            dishDto.setCategoryName(category.getName());
            /*
            放入
             */
            dishDtoList.add(dishDto);




        }
        dishdtoPage.setRecords(dishDtoList);
        return dishdtoPage;



    }
    public void UpdateStatus(long id,Integer status)
    {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);
        this.updateById(dish);
    }
    public R<DishDto> showOne(Long id)
    {
        Dish byId = this.getById(id);


        DishDto dishDto = new DishDto();//建立一个完整DishDto
        BeanUtils.copyProperties(byId,dishDto);//再一次复制
            /*
            查询
             */
        Long categoryId = byId.getCategoryId();
        Category category = categoryImpl.getById(categoryId);
        dishDto.setCategoryName(category.getName());
            /*
            放入
             */
        return R.success(dishDto);
    }

    public R<String> updateOne(DishDto dishDto)
    {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDto,dish);
        this.updateById(dish);//更新dish


        //更新dishflavor，先清理再添加


        List<DishFlavor> flavors = dishDto.getFlavors();
        QueryWrapper<DishFlavor> wrapper=new QueryWrapper<>();
        wrapper.eq("dish_id",dish.getId());
        dishFlavorImpl.remove(wrapper);

        for ( DishFlavor df:flavors
             )
        {
            df.setDishId(dish.getId());
            dishFlavorImpl.save(df);



        }


        return R.success("修改成功");
    }

    public List<Dish> listDishes(Long categoryId)
    {

        QueryWrapper<Dish> wrapper = new QueryWrapper<>();
        wrapper.eq("category_id",categoryId);
        return this.list(wrapper);
    }
}
