package com.itheima.reggie_take_out.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie_take_out.entity.Category;
import com.itheima.reggie_take_out.mapper.CategoryMapper;
import com.itheima.reggie_take_out.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Denny
 * @create 2022-08-18 17:22
 */
@Slf4j
@Service
public class CategoryImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService
{
    @Autowired
    private DishImpl dish;
    @Autowired
    private SetmealImpl setmeal;
    public Page<Category> RefreshPage(int page,int pageSize)
    {
        QueryWrapper wrapper=new QueryWrapper();
        wrapper.eq("is_deleted",0);//判断记录是否被删除
        wrapper.orderByAsc("sort");
        Page<Category> categoryPage = new Page<>(page, pageSize);
        return this.page(categoryPage,wrapper);
    }
    public boolean deleteOne(Long ids)//本质上是修改
    {
        if(dish.selectOneByIds(ids)&&setmeal.selectOneByIds(ids))
        {
            Category category = new Category();
            category.setId(ids);
            category.setIsDeleted(1);
            this.updateById(category);
            return true;

        }
        else return false;
    }
    public void updateOne(Category category,HttpServletRequest request)
    {
        /*category.setUpdateUser((Long)request.getSession().getAttribute("employee"));
        category.setUpdateTime(LocalDateTime.now());*/
        this.updateById(category);
    }
    public List<Category> listCategories(Integer type)
    {
        QueryWrapper<Category> wrapper=new QueryWrapper<>();

            wrapper.eq("type",type);
            wrapper.orderByAsc("sort").orderByAsc("update_time");
            return this.list(wrapper);




    }

}
