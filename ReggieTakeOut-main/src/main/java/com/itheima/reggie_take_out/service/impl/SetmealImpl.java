package com.itheima.reggie_take_out.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie_take_out.entity.Dish;
import com.itheima.reggie_take_out.entity.Setmeal;
import com.itheima.reggie_take_out.mapper.SetmealMapper;
import com.itheima.reggie_take_out.service.SetmealService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Denny
 * @create 2022-08-19 15:39
 */
@Service
public class SetmealImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService
{
        public boolean selectOneByIds(Long ids)
    {
        QueryWrapper<Setmeal> wrapper=new QueryWrapper<>();
        wrapper.eq("category_id",ids);

        if(this.count(wrapper)==0)
        {
            return true;
        }

        else return false;
    }

    public void UpdateStatus(long l, Integer statusValue)
    {
        Setmeal setmeal = new Setmeal();
        setmeal.setStatus(statusValue);
        setmeal.setId(l);
        this.updateById(setmeal);
    }
}
