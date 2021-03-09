package com.how2java.tmall.controller;

import com.how2java.tmall.pojo.Category;
import com.how2java.tmall.service.CategoryService;
import com.how2java.tmall.util.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("")
public class CategoryController {
    @Autowired
    CategoryService categoryService;

    @RequestMapping("admin_category_list")
    public String list(Model model,Page page){
        List<Category> cs= categoryService.list(page);//获取当前页的分类集合
        int total = categoryService.total();//获取分类总数
        page.setTotal(total);//为分页对象设置总数
        model.addAttribute("cs", cs);//把分类集合放在cs中
        model.addAttribute("page", page);//把分页对象放在page中
        return "admin/listCategory";//跳转页面
    }

}