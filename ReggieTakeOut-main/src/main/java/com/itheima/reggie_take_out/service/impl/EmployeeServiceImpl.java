package com.itheima.reggie_take_out.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie_take_out.entity.Employee;
import com.itheima.reggie_take_out.mapper.EmployeeMapper;
import com.itheima.reggie_take_out.service.EmployeeService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

/**
 * @author Denny
 * @create 2022-08-13 17:22
 */
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper,Employee> implements EmployeeService
//继承ServiceImp为了override各种实现EmployeeService的抽象方法
{
    public Employee login(Employee employee)
    {
        //2、查询。老师使用了lambdaquerywrapper，但我使用querywrapper更好看懂。
        QueryWrapper<Employee> wrapper=new QueryWrapper<>();//创建打包器
        wrapper.eq("username",employee.getUsername());//把条件添加入打包器，但未查询
        return this.getOne(wrapper);//用这个类的对象的方法，根据打包器，查询一条（因为用户名无重复）。
        // this.getOne来自IService。ServiceImpl实现了ISerivice。
    }
    public Page CreatePage(int page,int pageSize,String name)
    {
        QueryWrapper<Employee> wrapper=new QueryWrapper<>();
        wrapper.orderByDesc("update_time");
        if(name!=null)
        {
            wrapper.like("name",name);
        }
        Page<Employee> employeePage = new Page<>(page, pageSize);
        return this.page(employeePage, wrapper);
    }
    public void DisorEnable(Employee employee)
    {
        this.updateById(employee);
    }

    /**
     * 这次通过id查
     * @param id
     * @return
     */
    public Employee QueryById(Long id)
    {

        QueryWrapper<Employee> wrapper=new QueryWrapper<>();
        wrapper.eq("id",id);
        return this.getOne(wrapper);

    }

}
