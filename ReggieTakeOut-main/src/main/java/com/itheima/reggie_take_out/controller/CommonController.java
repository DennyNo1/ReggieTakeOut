package com.itheima.reggie_take_out.controller;

import com.itheima.reggie_take_out.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/**
 * @author Denny
 * @create 2022-08-19 21:35
 */
@Slf4j//日志
@RestController
@RequestMapping("/common")
public class CommonController
{
    @Value("${reggie.path}")//自己得去application.yml添加
    private String Path;
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) //直接能收到文件
    {
        log.info("已正确映射");

        //1.改名字
        /*
        这两步就为了取个后缀名.jpg
         */
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        /*
        UUID+后缀名，成新文件名

         */
        String fileName = UUID.randomUUID().toString() + suffix;


        //2.转存地点
        try {
            file.transferTo(new File(Path+fileName));//存放路径+文件名=文件完全路径
        } catch (IOException e)
        {
            log.info("异常处理");
            e.printStackTrace();
        }
        return R.success(fileName);//返回文件名，用于其他控制器，存入数据库


    }

    /**
     * 服务器文件通过HttpServletResponse传给浏览器
     * @param name
     * @param response
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response)
    {
        File file = new File(Path + name);
        try
        {
            FileInputStream fileInputStream = new FileInputStream(file);//
            ServletOutputStream outputStream = response.getOutputStream();//输出流
            response.setContentType("image/jpeg");//估计后面有用
            byte []buffer=new byte[1024];
            int len;
            while ((len=fileInputStream.read(buffer))!=-1)
            {

                outputStream.write(buffer,0,len);
                outputStream.flush();
            }
            fileInputStream.close();
            outputStream.close();

        }

        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }


    }
}
