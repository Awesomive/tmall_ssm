package com.how2java.tmall.controller;

import com.how2java.tmall.pojo.Category;
import com.how2java.tmall.pojo.User;
import com.how2java.tmall.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("")
public class ForeController {
    @Autowired
    CategoryService categoryService;
    @Autowired
    ProductService productService;
    @Autowired
    UserService userService;
    @Autowired
    ProductImageService productImageService;
    @Autowired
    PropertyValueService propertyValueService;
    @Autowired
    OrderService orderService;
    @Autowired
    OrderItemService orderItemService;

    @RequestMapping("forehome")
    public String home(Model model) {
        //查询所有分类
        List<Category> cs= categoryService.list();
        //为这些分类填充产品集合
        productService.fill(cs);
        //为这些分类填充推荐产品集合
        productService.fillByRow(cs);
        model.addAttribute("cs", cs);
        //服务端跳转到home.jsp
        return "fore/home";
    }

    @RequestMapping("foreregister")
    public String register(Model model,User user) {
        String name =  user.getName();
        name = HtmlUtils.htmlEscape(name);
        user.setName(name);
        boolean exist = userService.isExist(name);

        if(exist){
            String m ="用户名已经被使用,不能使用";
            model.addAttribute("msg", m);

            return "fore/register";
        }
        userService.add(user);

        return "redirect:registerSuccessPage";
    }
    @RequestMapping("forelogin")
    //获取账号密码
    public String login(@RequestParam("name") String name, @RequestParam("password") String password, Model model, HttpSession session) {
        //把账号通过HtmlUtils.htmlEscape进行转义,注册的时候，ForeController.register()，就进行了转义，所以这里也需要转义。
        //恶意注册的时候，会使用诸如 <script>alert('papapa')</script> 这样的名称，会导致网页打开就弹出一个对话框。 那么在转义之后，就没有这个问题了。
        name = HtmlUtils.htmlEscape(name);
        //根据账号和密码获取User对象
        User user = userService.get(name,password);

        //如果对象为空，则服务端跳转回login.jsp，也带上错误信息，并且使用 loginPage.jsp 中的办法显示错误信息
        if(null==user){
            model.addAttribute("msg", "账号密码错误");
            return "fore/login";
        }
        //如果对象存在，则把对象保存在session中，并客户端跳转到首页"forehome"
        session.setAttribute("user", user);
        return "redirect:forehome";
    }
}