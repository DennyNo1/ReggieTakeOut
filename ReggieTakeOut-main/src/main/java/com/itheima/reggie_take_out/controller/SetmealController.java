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
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Denny
 * @create 2022-08-24 15:37
 */
@Slf4j//日志
@RestController
@RequestMapping("/setmeal")
public class SetmealController
{
    @Autowired
    private SetmealImpl setmealImpl;
    @Autowired
    private SetmealDishImpl setmealDishImpl;
    @Autowired
    private CategoryImpl categoryImpl;
    @Autowired
    private DishImpl dishImpl;
    @PostMapping
    public R<String> addSetmeal(@RequestBody SetmealDto setmealDto)
    {
        //1、将记录存入setmeal
        log.info(setmealDto.getCategoryId().toString());
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDto,setmeal);
        setmealImpl.save(setmeal);
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
    public R<String> deleteDish(@RequestParam(value = "ids") String ids )
    {

        log.info(ids);
        String[] split = ids.split(",");
        for (String s:split
        )
        {
            long l = Long.parseLong(s);
            Setmeal setmeal = new Setmeal();
            setmeal.setId(l);
            setmeal.setIsDeleted(1);
            setmealImpl.updateById(setmeal);

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
    @PutMapping
    public R<String> UpdateSetmeal(@RequestBody SetmealDto setmealDto)
    {
        log.info("参数接收成功");
        //这里最多修改2个表。
        //1.修改setmeal
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDto,setmeal);


        setmealImpl.updateById(setmeal);

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
    @GetMapping("/list")
    public R<List<SetmealDto>> ListSetmeal(Setmeal setmeal)//目前会传进来categoryid，status，name
    {
        QueryWrapper<Setmeal> wrapper = new QueryWrapper<>();
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
        return R.success(setmealDtos);
    }

}
