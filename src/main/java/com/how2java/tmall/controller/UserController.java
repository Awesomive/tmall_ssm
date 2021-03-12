package com.how2java.tmall.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.how2java.tmall.pojo.User;
import com.how2java.tmall.service.UserService;
import com.how2java.tmall.util.Page;

@Controller
@RequestMapping("")
public class UserController {
    @Autowired
    UserService userService;

    @RequestMapping("admin_user_list")
    ////获取分页对象
    public String list(Model model, Page page){
        //设置分页信息
        PageHelper.offsetPage(page.getStart(),page.getCount());

        //查询用户集合
        List<User> us= userService.list();

        //通过PageInfo获取总数，并设置在page对象上
        int total = (int) new PageInfo<>(us).getTotal();
        page.setTotal(total);

        //把用户集合设置到model的"us"属性上
        model.addAttribute("us", us);
        //把分页对象设置到model的"page"属性上
        model.addAttribute("page", page);

        //服务端跳转到admin/listUser.jsp页面
        return "admin/listUser";
    }

}