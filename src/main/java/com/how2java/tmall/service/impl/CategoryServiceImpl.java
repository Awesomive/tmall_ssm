package com.how2java.tmall.service.impl;
import com.how2java.tmall.mapper.CategoryMapper;
import com.how2java.tmall.pojo.Category;
import com.how2java.tmall.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class CategoryServiceImpl  implements CategoryService {
//    通过自动装配@Autowired引入CategoryMapper ，在list方法中调用CategoryMapper 的list方法.
    @Autowired
    CategoryMapper categoryMapper;
    @Override
    public List<Category> list(){
        return categoryMapper.list();
    }

}