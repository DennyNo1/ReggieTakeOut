package com.itheima.reggie_take_out.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie_take_out.common.R;
import com.itheima.reggie_take_out.entity.Employee;
import com.itheima.reggie_take_out.service.EmployeeService;
import com.itheima.reggie_take_out.service.impl.EmployeeServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.LoginContext;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * @author Denny
 * @create 2022-08-13 17:53
 */
@Slf4j//日志
@RestController
@RequestMapping("/employee")
public class EmployeeController
{
    @Autowired
    private EmployeeServiceImpl employeeServiceImpl;//用于之后调用？

    /**
     * 登录
     * @param employee
     * @param request
     * @return
     */
    @PostMapping("/login")//按理说，具体的操作过程应该写在ServiceImpl，但是这里写在了controller。
    public R<Employee> login(@RequestBody Employee employee, HttpServletRequest request)
    {

        //1、密码加密
        String password = employee.getPassword();
        String s = DigestUtils.md5DigestAsHex(password.getBytes());//工具类MD5加密
        //2、查询放在对应ServiceImpl,这里只接受结果
        Employee one = employeeServiceImpl.login(employee);



        //3、判断
        if(one==null)
        {
            return R.error("无该用户，登录失败");

        }
        if(!one.getPassword().equals(s))
        {
            return R.error("密码错误，登录失败");

        }
        if(one.getStatus()==0)
        {
            return R.error("员工被禁用，登录失败");

        }
        log.info("{}",one.getId());
        request.getSession().setAttribute("employee",one.getId());//将员工id存入session，为了以后用
        log.info("用户id为：{}",request.getSession().getAttribute("employee"));

        return R.success(one);//（）是数据库内查出的完整对象，需要发送给前端做更多处理
    }

    /**
     *退出系统
     * @param request
     * @return
     */


    @PostMapping("/logout")//按理说，具体的操作过程应该写在ServiceImpl，但是这里写在了controller。
    public R<String> out(HttpServletRequest request)
    {
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 员工添加
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> addEmployee(@RequestBody Employee employee,HttpServletRequest request)
    {
        //1、帮新员工填写前端未写的默认信息,有默认就不用填

        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        /*已使用公共字段填充
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        employee.setCreateUser((Long)request.getSession().getAttribute("employee"));
        employee.setUpdateUser((Long)request.getSession().getAttribute("employee"));*/
        //2、使用之前的serviceImpl的save()
        employeeServiceImpl.save(employee);



        return R.success("新增员工成功");//成功实际上是吧（）实参装入data属性
    }

    /**
     * 分页显示
     * @param page
     * @param pageSize
     * @param name
     * @return
     */

    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name)

    {
        log.info("{},{},{}",page,pageSize,name);
        return R.success(employeeServiceImpl.CreatePage(page,pageSize,name));//按照前端设计，往R.success里返回一个Page对象即可

    }

    /**
     * 实际效果是，更新员工信息
     * @param employee
     * @param request
     * @return
     */
    @PutMapping
    public R<String> DisorEnable(@RequestBody Employee employee,HttpServletRequest request)
    {
        log.info("开始操作");
        //还要update修改时间，和修改者
        /*employee.setUpdateUser((Long) request.getSession().getAttribute("employee"));
        employee.setUpdateTime(LocalDateTime.now());*/
        employeeServiceImpl.DisorEnable(employee);
        return R.success("操作成功");
    }

    /**
     * 修改员工时显示所有信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> QueryOne(@PathVariable Long id)
    {
        return R.success(employeeServiceImpl.QueryById(id));
    }


}
