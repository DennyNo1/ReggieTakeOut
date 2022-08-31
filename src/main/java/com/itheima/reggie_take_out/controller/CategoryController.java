package com.itheima.reggie_take_out.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie_take_out.DTO.SetmealDto;
import com.itheima.reggie_take_out.common.R;
import com.itheima.reggie_take_out.entity.Category;
import com.itheima.reggie_take_out.service.impl.CategoryImpl;
import com.itheima.reggie_take_out.service.impl.DishImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Denny
 * @create 2022-08-18 17:16
 */
@Slf4j//日志
@RestController
@RequestMapping("/category")
public class CategoryController
{
    @Autowired
    private CategoryImpl categoryImpl;


    /**
     * 页面刷新
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page> categoryPage(int page,int pageSize)
    {
        log.info("方法启动");
        return R.success(categoryImpl.RefreshPage(page,pageSize));
    }

    /**
     * 删除category
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> deleteOne(long ids)
    {
        if(categoryImpl.deleteOne(ids))
        {
            return R.success("删除成功");
        }
        else return R.error("该类下有菜品或套餐关联，删除失败");
        //老师把删除失败，作为一种异常在serviceimpl抛出，在exceptionhandler里处理后在R.error
        //逻辑上，是这样。可是现有数据库，并没有外键，所以根据实际操作，我觉得没有必要。

    }

    /**
     * 修改category
     * @param category
     * @param request
     * @return
     */
    @PutMapping
    public R<String> updateOne(@RequestBody Category category,HttpServletRequest request)
    {
        categoryImpl.updateOne(category,request);
        return R.success("修改成功");
    }
    @PostMapping
    public R<String> add(@RequestBody Category category)
    {
        category.setIsDeleted(0);
        categoryImpl.save(category);
        return R.success("添加成功");
    }


    /**
     * 列表显示所有category，或者category和对应的dish

     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> listCategories( @RequestParam(value = "type",required = false) Integer type)
    {

            if(type==null)
                return R.success(categoryImpl.list());
            return R.success(categoryImpl.listCategories(type));





    }


}
