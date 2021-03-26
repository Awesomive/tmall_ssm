package com.how2java.tmall.controller;

import com.github.pagehelper.PageHelper;
import com.how2java.tmall.pojo.*;
import com.how2java.tmall.service.*;
import comparator.*;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
    @Autowired
    ReviewService reviewService;

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

    @RequestMapping("forelogout")
    public String logout( HttpSession session) {
        //在session中去掉"user"
        session.removeAttribute("user");
        //客户端跳转到首页
        return "redirect:forehome";
    }

    @RequestMapping("foreproduct")
    //获取参数pid
    public String product( int pid, Model model) {
        //根据pid获取Product 对象p
        Product p = productService.get(pid);

        //根据对象p，获取这个产品对应的单个图片集合
        List<ProductImage> productSingleImages = productImageService.list(p.getId(), ProductImageService.type_single);
        //根据对象p，获取这个产品对应的详情图片集合
        List<ProductImage> productDetailImages = productImageService.list(p.getId(), ProductImageService.type_detail);
        p.setProductSingleImages(productSingleImages);
        p.setProductDetailImages(productDetailImages);

        //获取产品的所有属性值
        List<PropertyValue> pvs = propertyValueService.list(p.getId());
        //获取产品对应的所有的评价
        List<Review> reviews = reviewService.list(p.getId());
        //设置产品的销量和评价数量
        productService.setSaleAndReviewNumber(p);
        //把上述取值放在request属性上
        model.addAttribute("reviews", reviews);
        model.addAttribute("p", p);
        model.addAttribute("pvs", pvs);
        //服务端跳转到 "product.jsp" 页面
        return "fore/product";
    }

    @RequestMapping("forecheckLogin")
    @ResponseBody
    //在上一步的ajax访问路径/forecheckLogin会导致ForeController.checkLogin()方法被调用。
    //获取session中的"user"对象
    //如果不为空，即表示已经登录，返回字符串"success"
    //如果为空，即表示未登录，返回字符串"fail"
    public String checkLogin( HttpSession session) {
        User user =(User)  session.getAttribute("user");
        if(null!=user)
            return "success";
        return "fail";
    }



    @RequestMapping("foreloginAjax")
    @ResponseBody
    //在上一步modal.jsp中，点击了登录按钮之后，访问路径/foreloginAjax,导致ForeController.loginAjax()方法被调用
    //1. 获取账号密码
    //2. 通过账号密码获取User对象
    //2.1 如果User对象为空，那么就返回"fail"字符串。
    //2.2 如果User对象不为空，那么就把User对象放在session中，并返回"success" 字符串
    public String loginAjax(@RequestParam("name") String name, @RequestParam("password") String password,HttpSession session) {
        name = HtmlUtils.htmlEscape(name);
        User user = userService.get(name,password);

        if(null==user){
            return "fail";
        }
        session.setAttribute("user", user);
        return "success";
    }

    @RequestMapping("forecategory")
    //获取参数cid
    public String category(int cid,String sort, Model model) {
        //根据cid获取分类Category对象 c
        Category c = categoryService.get(cid);
        //为c填充产品
        productService.fill(c);
        //为产品填充销量和评价数据
        productService.setSaleAndReviewNumber(c.getProducts());

        //获取参数sort
        //如果sort==null，即不排序
        if(null!=sort){
            //如果sort!=null，则根据sort的值，从5个Comparator比较器中选择一个对应的排序器进行排序
            switch(sort){
                case "review":
                    Collections.sort(c.getProducts(),new ProductReviewComparator());
                    break;
                case "date" :
                    Collections.sort(c.getProducts(),new ProductDateComparator());
                    break;

                case "saleCount" :
                    Collections.sort(c.getProducts(),new ProductSaleCountComparator());
                    break;

                case "price":
                    Collections.sort(c.getProducts(),new ProductPriceComparator());
                    break;

                case "all":
                    Collections.sort(c.getProducts(),new ProductAllComparator());
                    break;
            }
        }

        //把c放在model中
        model.addAttribute("c", c);
        //服务端跳转到 category.jsp
        return "fore/category";
    }


    //通过search.jsp或者simpleSearch.jsp提交数据到路径 /foresearch， 导致ForeController.search()方法被调用
    @RequestMapping("foresearch")
    //获取参数keyword
    public String search( String keyword,Model model){

        //根据keyword进行模糊查询，获取满足条件的前20个产品
        PageHelper.offsetPage(0,20);
        List<Product> ps= productService.search(keyword);
        //为这些产品设置销量和评价数量
        productService.setSaleAndReviewNumber(ps);
        //把产品结合设置在model的"ps"属性上
        model.addAttribute("ps",ps);
        //服务端跳转到 searchResult.jsp 页面
        return "fore/searchResult";
    }


    //通过上个步骤访问的地址 /forebuyone 导致ForeController.buyone()方法被调用
    @RequestMapping("forebuyone")
    //获取参数pid
    //获取参数num
    public String buyone(int pid, int num, HttpSession session) {
        //根据pid获取产品对象p
        Product p = productService.get(pid);
        int oiid = 0;

        //从session中获取用户对象user
        User user =(User)  session.getAttribute("user");
        boolean found = false;
        //如果已经存在这个产品对应的OrderItem，并且还没有生成订单，即还在购物车中。 那么就应该在对应的OrderItem基础上，调整数量
        //基于用户对象user，查询没有生成订单的订单项集合
        List<OrderItem> ois = orderItemService.listByUser(user.getId());
        //遍历这个集合
        for (OrderItem oi : ois) {
            if(oi.getProduct().getId().intValue()==p.getId().intValue()){
                //如果产品是一样的话，就进行数量追加
                oi.setNumber(oi.getNumber()+num);
                //使用orderItemService进行更新orderItem数量
                orderItemService.update(oi);
                //将found改为true，跳过下一个if判断
                found = true;
                //获取这个订单项的 id
                oiid = oi.getId();
                break;
            }
        }

        //如果不存在对应的OrderItem,那么就新增一个订单项OrderItem
        if(!found){
            //生成新的订单项
            OrderItem oi = new OrderItem();
            //设置数量，用户和产品
            oi.setUid(user.getId());
            oi.setNumber(num);
            oi.setPid(pid);

            //插入到数据库
            orderItemService.add(oi);
            //插入到数据库
            oiid = oi.getId();
        }
        //最后， 基于这个订单项id客户端跳转到结算页面/forebuy
        return "redirect:forebuy?oiid="+oiid;
    }

    //点立即购买最后，客户端跳转到结算路径： "@forebuy?oiid="+oiid;导致ForeController.buy()方法被调用
    @RequestMapping("forebuy")
    //通过字符串数组获取参数oiid
    //根据购物流程环节与表关系，结算页面还需要显示在购物车中选中的多条OrderItem数据
    //所以为了兼容从购物车页面跳转过来的需求，要用字符串数组获取多个oiid
    public String buy( Model model,String[] oiid,HttpSession session){
        //准备一个泛型是OrderItem的集合ois
        List<OrderItem> ois = new ArrayList<>();
        float total = 0;

        //根据前面步骤获取的oiids，从数据库中取出OrderItem对象，并放入ois集合中
        for (String strid : oiid) {
            int id = Integer.parseInt(strid);
            OrderItem oi= orderItemService.get(id);
            //累计这些ois的价格总数，赋值在total上
            total +=oi.getProduct().getPromotePrice()*oi.getNumber();
            ois.add(oi);
        }

        //把订单项集合放在session的属性 "ois" 上
        session.setAttribute("ois", ois);
        //把总价格放在 model的属性 "total" 上
        model.addAttribute("total", total);
        return "fore/buy";
    }



    @RequestMapping("foreaddCart")
    @ResponseBody
    //上一步访问地址/foreaddCart导致ForeController.addCart()方法被调用
    //addCart()方法和立即购买中的 ForeController.buyone()步骤做的事情是一样的，区别在于返回不一样
    //获取参数pid
    //获取参数num
    public String addCart(int pid, int num, Model model,HttpSession session) {
        //根据pid获取产品对象p
        Product p = productService.get(pid);
        //从session中获取用户对象user
        User user =(User)  session.getAttribute("user");
        boolean found = false;

        //如果已经存在这个产品对应的OrderItem，并且还没有生成订单，即还在购物车中。 那么就应该在对应的OrderItem基础上，调整数量
        //基于用户对象user，查询没有生成订单的订单项集合
        List<OrderItem> ois = orderItemService.listByUser(user.getId());
        //遍历这个集合
        for (OrderItem oi : ois) {
            //如果产品是一样的话，就进行数量追加
            if(oi.getProduct().getId().intValue()==p.getId().intValue()){
                oi.setNumber(oi.getNumber()+num);
                orderItemService.update(oi);
                found = true;
                break;
            }
        }
        //如果不存在对应的OrderItem,那么就新增一个订单项OrderItem
        if(!found){
            //生成新的订单项
            OrderItem oi = new OrderItem();
            //设置数量，用户，产品
            oi.setUid(user.getId());
            oi.setNumber(num);
            oi.setPid(pid);
            //插入到数据库
            orderItemService.add(oi);
        }
        //与ForeController.buyone() 客户端跳转到结算页面不同的是， 最后返回字符串"success"，表示添加成功
        return "success";
    }


    @RequestMapping("forecart")
    public String cart( Model model,HttpSession session) {
        //通过session获取当前用户
        //一定要登录才访问，否则拿不到用户对象,会报错
        User user =(User)  session.getAttribute("user");
        //获取为这个用户关联的订单项集合 ois
        List<OrderItem> ois = orderItemService.listByUser(user.getId());
        //把ois放在model中
        model.addAttribute("ois", ois);
        //服务端跳转到cart.jsp
        return "fore/cart";
    }


     //点击购物车增加或者减少按钮后
    @RequestMapping("forechangeOrderItem")
    @ResponseBody
    //获取pid和number
    public String changeOrderItem( Model model,HttpSession session, int pid, int number) {
        //判断用户是否登录
        User user =(User)  session.getAttribute("user");
        if(null==user)
            return "fail";

        //遍历出用户当前所有的未生成订单的OrderItem
        List<OrderItem> ois = orderItemService.listByUser(user.getId());
        for (OrderItem oi : ois) {
            //根据pid找到匹配的OrderItem，并修改数量后更新到数据库
            if(oi.getProduct().getId().intValue()==pid){
                oi.setNumber(number);
                orderItemService.update(oi);
                break;
            }

        }
        //返回字符串"success"
        return "success";
    }

    //购物车删除按钮
    @RequestMapping("foredeleteOrderItem")
    @ResponseBody
    //获取oiid
    public String deleteOrderItem( Model model,HttpSession session,int oiid){
        //判断用户是否登录
        User user =(User)  session.getAttribute("user");
        if(null==user)
            return "fail";

        //删除oiid对应的OrderItem数据
        orderItemService.delete(oiid);
        //返回字符串"success"
        return "success";
    }


    @RequestMapping("forecreateOrder")
    public String createOrder( Model model,Order order,HttpSession session){
        //从session中获取user对象
        User user =(User)  session.getAttribute("user");
        //根据当前时间加上一个4位随机数生成订单号
        String orderCode = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + RandomUtils.nextInt(10000);
        //通过参数Order接受地址，邮编，收货人，用户留言等信息
        order.setOrderCode(orderCode);
        order.setCreateDate(new Date());
        order.setUid(user.getId());
        order.setStatus(OrderService.waitPay);
        List<OrderItem> ois= (List<OrderItem>)  session.getAttribute("ois");

        //根据上述参数，创建订单对象
        float total =orderService.add(order,ois);
        //客户端跳转到确认支付页forealipay，并带上订单id和总金额
        //PageController.alipay()方法被调用
        return "redirect:forealipay?oid="+order.getId() +"&total="+total;
    }

    @RequestMapping("forepayed")
    //获取参数oid
    public String payed(int oid, float total, Model model) {
        //根据oid获取到订单对象order
        Order order = orderService.get(oid);
        //修改订单对象的状态和支付时间
        order.setStatus(OrderService.waitDelivery);
        order.setPayDate(new Date());
        //更新这个订单对象到数据库
        orderService.update(order);
        //把这个订单对象放在model的属性"o"上
        model.addAttribute("o", order);
        //服务端跳转到payed.jsp
        return "fore/payed";
    }

}





