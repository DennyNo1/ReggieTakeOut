package com.itheima.reggie_take_out.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie_take_out.DTO.DishDto;
import com.itheima.reggie_take_out.DTO.SetmealDto;
import com.itheima.reggie_take_out.common.R;
import com.itheima.reggie_take_out.entity.*;
import com.itheima.reggie_take_out.service.impl.CategoryImpl;
import com.itheima.reggie_take_out.service.impl.DishImpl;
import com.itheima.reggie_take_out.service.impl.SetmealDishImpl;
import com.itheima.reggie_take_out.service.impl.SetmealImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Denny
 * @create 2022-08-24 15:37
 */
@Slf4j//日志
@RestController
@RequestMapping("/setmeal")
//这个控制类使用springcache缓存
public class SetmealController
{
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private SetmealImpl setmealImpl;
    @Autowired
    private SetmealDishImpl setmealDishImpl;
    @Autowired
    private CategoryImpl categoryImpl;
    @Autowired
    private DishImpl dishImpl;
    @Autowired
    private RedisTemplate redisTemplate;
    @PostMapping
    @CacheEvict(value = "setmeal",allEntries = true)//无法精准定位record，只能整组删除
    public R<String> addSetmeal(@RequestBody SetmealDto setmealDto)
    {
        //1、将记录存入setmeal
        log.info(setmealDto.getCategoryId().toString());
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDto,setmeal);
        setmealImpl.save(setmeal);
/*        String key=setmeal.getCategoryId().toString()+"1";
        redisTemplate.delete(key);*/
        //2、取出上述记录的id
        QueryWrapper<Setmeal> wrapper = new QueryWrapper<>();
        wrapper.eq("name",setmeal.getName());
        Setmeal fullRecord = setmealImpl.getOne(wrapper);
        //3、将多条记录cunrusetmealdish，存入前给记录的setmealid赋值
        for (SetmealDish setmealDish:setmealDto.getSetmealDishes()
             )
        {
            setmealDish.setSetmealId(fullRecord.getId());
            setmealDishImpl.save(setmealDish);


        }
        return R.success("添加套餐成功");

    }

    /**
     * 后台界面显示
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page<SetmealDto>> RefreshPage(int page,int pageSize,String name)
    {
        Page<Setmeal> setmealPage = new Page<>(page, pageSize);
        QueryWrapper<Setmeal> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted",0);
        if(name!=null)
        {
            wrapper.like("name",name);
        }
        Page<Setmeal> page1 = setmealImpl.page(setmealPage, wrapper);
        Page<SetmealDto> page2 = new Page<>(page, pageSize);
        BeanUtils.copyProperties(page1,page2,"records");//这里必须忽视，因为List不同了吧
        List<Setmeal> list1=page1.getRecords();
        ArrayList<SetmealDto> list2 = new ArrayList<>();
        for (Setmeal s:list1
             )
        {
            SetmealDto s2 = new SetmealDto();
            BeanUtils.copyProperties(s,s2);
            Long categoryId = s.getCategoryId();
            Category byId = categoryImpl.getById(categoryId);
            s2.setCategoryName(byId.getName());
            list2.add(s2);


        }
        page2.setRecords(list2);



        return R.success(page2);

    }
    @DeleteMapping
    //这里多一个判断，未停售，无法删除。所以与缓存无关，因为停售也不会显示在客户界面。
    public R<String> deleteDish(@RequestParam(value = "ids") String ids )
    {

        log.info(ids);
        String[] split = ids.split(",");
        for (String s:split
        )
        {
            long l = Long.parseLong(s);
            Setmeal byId = setmealImpl.getById(l);
            if(byId.getStatus()==1)
                return R.error("该商品处于售卖状态，无法删除！");

            Setmeal setmeal = new Setmeal();
            setmeal.setId(l);
            setmeal.setIsDeleted(1);


            setmealImpl.updateById(setmeal);
           /*
            String key=byId.getCategoryId().toString()+"1";
            redisTemplate.delete(key);*/

            //修改映射的setmealdish记录
            QueryWrapper<SetmealDish> wrapper = new QueryWrapper<>();
            wrapper.eq("setmeal_id",l);
            List<SetmealDish> list = setmealDishImpl.list(wrapper);
            for (SetmealDish setmealDish:list)
            {
                setmealDish.setIsDeleted(1);
            }
            setmealDishImpl.updateBatchById(list);


        }

        return R.success("删除成功");
    }
    @CacheEvict(value = "setmeal",allEntries = true)//无法精准定位record，只能整组删除
    @PostMapping("/status/{statusValue}")
    public R<String> stopSellingDish(@RequestParam(value = "ids") String ids,@PathVariable Integer statusValue )
    {

        log.info(ids);
        String[] split = ids.split(",");
        for (String s:split
        )
        {
            long l = Long.parseLong(s);
            setmealImpl.UpdateStatus(l,statusValue);
/*
            Setmeal byId = setmealImpl.getById(l);
            String key=byId.getCategoryId().toString()+"1";
            redisTemplate.delete(key);
*/


        }

        return R.success("修改成功");
    }


    @GetMapping("/{id}")
    public R<SetmealDto> ListSetmeal(@PathVariable Long id)
    {
        log.info(id.toString());
        Setmeal setmeal = setmealImpl.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);

        //返回List<setmealdish>
        QueryWrapper<SetmealDish> wrapper = new QueryWrapper<>();
        wrapper.eq("setmeal_id",id);
        List<SetmealDish> list = setmealDishImpl.list(wrapper);
        setmealDto.setSetmealDishes(list);

        //返回categoryname
        Long categoryId = setmeal.getCategoryId();
        setmealDto.setCategoryName(categoryImpl.getById(categoryId).getName());

        return R.success(setmealDto);
    }
    @CacheEvict(value = "setmeal",allEntries = true)//无法精准定位record，只能整组删除
    @PutMapping
    public R<String> UpdateSetmeal(@RequestBody SetmealDto setmealDto)
    {
        log.info("参数接收成功");
        //这里最多修改2个表。
        //1.修改setmeal
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDto,setmeal);


        setmealImpl.updateById(setmeal);

/*        String key=setmeal.getCategoryId().toString()+"1";
        redisTemplate.delete(key);*/

        //2.修改setmealdish
        Long id = setmeal.getId();
        QueryWrapper<SetmealDish> wrapper = new QueryWrapper<>();
        wrapper.eq("setmeal_id",id);
        setmealDishImpl.remove(wrapper);
        List<SetmealDish> list = setmealDto.getSetmealDishes();
        for (SetmealDish s:list
             )
        {
            s.setSetmealId(setmeal.getId());
            setmealDishImpl.save(s);

        }
        return R.success("修改成功");
    }
    @Cacheable(value = "setmeal",key = "#setmeal.categoryId+#setmeal.status")
    @GetMapping("/list")
    public R<List<SetmealDto>> ListSetmeal(Setmeal setmeal)//目前会传进来categoryid，status，name
    {
        //先查询redis
/*        String key=setmeal.getCategoryId().toString()+setmeal.getStatus().toString();
        List<SetmealDto> redisOne = (List<SetmealDto>)redisTemplate.opsForValue().get(key);

        if(redisOne!=null)
        {
            log.info("由redis显示");
            return R.success(redisOne);

        }*/
        QueryWrapper<Setmeal> wrapper = new QueryWrapper<>();
        wrapper.eq("is_deleted",0);
        if (setmeal.getName() != null) {

            wrapper.like("name", setmeal.getName());
        }
        if (setmeal.getCategoryId() != null) {
            wrapper.eq("category_id", setmeal.getCategoryId());
        }
        if (setmeal.getStatus() != null) {
            wrapper.eq("status", 1);
        }
        List<Setmeal> list = setmealImpl.list(wrapper);
        //再查询每个dish的口味等，包装成dishdto
        List<SetmealDto> setmealDtos = new ArrayList<SetmealDto>();
        for (Setmeal s : list
        ) {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(s, setmealDto);
            setmealDto.setCategoryName(categoryImpl.getById(s.getCategoryId()).getName());

            List<SetmealDish> setmealDishes = setmealDishImpl.list(new QueryWrapper<SetmealDish>().eq("setmeal_id", s.getId()));
            setmealDto.setSetmealDishes(setmealDishes);
            setmealDtos.add(setmealDto);

        }
        /*redisTemplate.opsForValue().set(key,setmealDtos,1, TimeUnit.HOURS);*/
        return R.success(setmealDtos);
    }


}
