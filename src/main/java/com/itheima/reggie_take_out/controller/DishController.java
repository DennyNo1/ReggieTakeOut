package com.itheima.reggie_take_out.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie_take_out.DTO.DishDto;
import com.itheima.reggie_take_out.common.R;
import com.itheima.reggie_take_out.entity.Dish;
import com.itheima.reggie_take_out.entity.DishFlavor;
import com.itheima.reggie_take_out.service.impl.CategoryImpl;
import com.itheima.reggie_take_out.service.impl.DishFlavorImpl;
import com.itheima.reggie_take_out.service.impl.DishImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Denny
 * @create 2022-08-23 10:35
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController
{
    @Autowired
    private DishImpl dishImpl;
    @Autowired
    private DishFlavorImpl dishFlavorImpl;
    @Autowired
    private CategoryImpl categoryImpl;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    public R<String> addDish(@RequestBody DishDto dishDto)
    {
        log.info("dish控制器已启动");
        String key = dishDto.getCategoryId().toString()+"1";
        redisTemplate.delete(key);
        dishImpl.save(dishDto);//数据库插入使用save
        List<DishFlavor> flavors = dishDto.getFlavors();

        for ( DishFlavor dishFlavor:flavors
             )
        {
            dishFlavor.setDishId(dishDto.getId());
            dishFlavorImpl.save(dishFlavor);
            
        }
        return R.success("新增菜品成功");
    }

    /**
     * 显示所有菜品
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<DishDto>> showAllDish(int page,int pageSize,String name)
    {

        return R.success(dishImpl.showAllDish(page,pageSize,name));
    }
    @PostMapping("/status/{statusValue}")
    public R<String> stopSellingDish(@RequestParam(value = "ids") String ids,@PathVariable Integer statusValue )
    {

        log.info(ids);
        String[] split = ids.split(",");
        for (String s:split
             )
        {
            long l = Long.parseLong(s);
            dishImpl.UpdateStatus(l,statusValue);
            Dish byId = dishImpl.getById(l);
            String key = byId.getCategoryId().toString()+"1";
            redisTemplate.delete(key);



        }

        return R.success("修改成功");
    }
    @DeleteMapping
    public R<String> deleteDish(@RequestParam(value = "ids") String ids )//
    {

        log.info(ids);
        String[] split = ids.split(",");
        for (String s:split
        )
        {
            long l = Long.parseLong(s);
            Dish dish = new Dish();
            dish.setId(l);
            dish.setIsDeleted(1);
            dishImpl.updateById(dish);
            Dish byId = dishImpl.getById(l);
            String key = byId.getCategoryId().toString()+"1";
            redisTemplate.delete(key);
            log.info(key);

        }

        return R.success("修改成功");
    }
    @GetMapping("/{id}")
    //这里得返回DishDto,而不是Dish
    public R<DishDto> showOne(@PathVariable Long id)
    {
        log.info(id.toString());
        return dishImpl.showOne(id);
    }
    @PutMapping
    //这里得返回DishDto,而不是Dish
    public R<String> updateOne(@RequestBody DishDto dishDto)
    {
        //redis清理相应category缓存
        String key = dishDto.getCategoryId().toString()+"1";
        redisTemplate.delete(key);


        log.info(dishDto.toString());

        return dishImpl.updateOne(dishDto);
    }
/*    @GetMapping("/list")
    public R<Object> listDished
            (@RequestParam(value = "categoryId",required = false) Long categoryId,@RequestParam(value = "name",required = false)String name)
    {
        if(categoryId!=null)
        {
            List<Dish> dishes = dishImpl.listDishes(categoryId);

            return R.success(dishes);
        }
        if(name!=null)
        {
            QueryWrapper<Dish> wrapper = new QueryWrapper<>();
            wrapper.like("name",name);
            return R.success(dishImpl.getOne(wrapper));
        }
        return R.error("未知错误");

    }*/
    @GetMapping("/list")
    public R<List<DishDto>> listDishDto(Dish dish)//目前会传进来categoryid，status，name
    {
        //先查询redis
        String key=dish.getCategoryId().toString()+dish.getStatus().toString();
        List<DishDto> redisOne = (List<DishDto>)redisTemplate.opsForValue().get(key);

        if(redisOne!=null)
        {
            log.info("由redis显示");
            return R.success(redisOne);

        }

        //再查询mysql
        QueryWrapper<Dish> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted",0);
        if(dish.getName()!=null) {

            wrapper.like("name", dish.getName());
        }
        if(dish.getCategoryId()!=null)
        {
            wrapper.eq("category_id",dish.getCategoryId());
        }
        if(dish.getStatus()!=null)
        {
            wrapper.eq("status",1);
        }
        List<Dish> list = dishImpl.list(wrapper);
        //再查询每个dish的口味等，包装成dishdto
        List<DishDto> dishDtoList = new ArrayList<DishDto>();
        for (Dish d: list
             )
        {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(d,dishDto);
            dishDto.setCategoryName(categoryImpl.getById(d.getCategoryId()).getName());

            List<DishFlavor> dishFlavorList = dishFlavorImpl.list(new QueryWrapper<DishFlavor>().eq("dish_id",d.getId()));
            dishDto.setFlavors(dishFlavorList);
            dishDtoList.add(dishDto);

        }
        //将mysql查出来的对象存入redis
        redisTemplate.opsForValue().set(key,dishDtoList,1, TimeUnit.HOURS);

        return R.success(dishDtoList);


    }

}
