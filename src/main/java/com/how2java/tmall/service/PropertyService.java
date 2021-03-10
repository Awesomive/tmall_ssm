package com.how2java.tmall.service;

import com.how2java.tmall.pojo.Property;

import java.util.List;

public interface PropertyService {
    void add(Property c);
    void delete(int id);
    void update(Property c);
    Property get(int id);
    //因为在业务上需要查询某个分类下的属性，所以list方法会带上对应分类的id
    List list(int cid);
}