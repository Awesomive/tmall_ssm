package com.how2java.tmall.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.how2java.tmall.pojo.Order;
import com.how2java.tmall.service.OrderItemService;
import com.how2java.tmall.service.OrderService;
import com.how2java.tmall.util.Page;

@Controller
@RequestMapping("")
public class OrderController {
    @Autowired
    OrderService orderService;
    @Autowired
    OrderItemService orderItemService;

    @RequestMapping("admin_order_list")
    public String list(Model model, Page page){
        //获取分页对象
        PageHelper.offsetPage(page.getStart(),page.getCount());

        //查询订单集合
        List<Order> os= orderService.list();

        //获取订单总数并设置在分页对象上
        int total = (int) new PageInfo<>(os).getTotal();
        page.setTotal(total);

        //借助orderItemService.fill()方法为这些订单填充上orderItems信息
        orderItemService.fill(os);

        //把订单集合和分页对象设置在model上
        model.addAttribute("os", os);
        model.addAttribute("page", page);

        //服务端跳转到admin/listOrder.jsp页面
        return "admin/listOrder";
    }

    //发货按钮链接跳转到admin_order_delivery
    @RequestMapping("admin_order_delivery")
    //注入订单对象
    public String delivery(Order o) throws IOException {
        //修改发货时间，设置发货状态
        o.setDeliveryDate(new Date());
        o.setStatus(OrderService.waitConfirm);

        //更新到数据库
        orderService.update(o);
        //客户端跳转到admin_order_list页面
        return "redirect:admin_order_list";
    }
}