package com.how2java.tmall.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.how2java.tmall.pojo.Product;
import com.how2java.tmall.pojo.ProductImage;
import com.how2java.tmall.service.ProductImageService;
import com.how2java.tmall.service.ProductService;
import com.how2java.tmall.util.ImageUtil;
import com.how2java.tmall.util.Page;
import com.how2java.tmall.util.UploadedImageFile;

@Controller
@RequestMapping("")
public class ProductImageController {
    @Autowired
    ProductService productService;

    @Autowired
    ProductImageService productImageService;

    @RequestMapping("admin_productImage_add")
    //通过pi对象接受type和pid的注入
    public String add(ProductImage pi, HttpSession session, UploadedImageFile uploadedImageFile) {
        //借助productImageService，向数据库中插入数据。
        productImageService.add(pi);
        String fileName = pi.getId() + ".jpg";
        String imageFolder;
        String imageFolder_small = null;
        String imageFolder_middle = null;

        //根据session().getServletContext().getRealPath( "img/productSingle")，定位到存放单个产品图片的目录
        //除了productSingle，还有productSingle_middle和productSingle_small。 因为每上传一张图片，都会有对应的正常，中等和小的三种大小图片，并且放在3个不同的目录下
        if (ProductImageService.type_single.equals(pi.getType())) {
            imageFolder = session.getServletContext().getRealPath("img/productSingle");
            imageFolder_small = session.getServletContext().getRealPath("img/productSingle_small");
            imageFolder_middle = session.getServletContext().getRealPath("img/productSingle_middle");
        } else {
            imageFolder = session.getServletContext().getRealPath("img/productDetail");
        }

        //文件命名以保存到数据库的产品图片对象的id+".jpg"的格式命名
        File f = new File(imageFolder, fileName);
        f.getParentFile().mkdirs();
        try {
            //通过uploadedImageFile保存文件
            uploadedImageFile.getImage().transferTo(f);
            //借助ImageUtil.change2jpg()方法把格式真正转化为jpg，而不仅仅是后缀名为.jpg
            BufferedImage img = ImageUtil.change2jpg(f);
            ImageIO.write(img, "jpg", f);

            //再借助ImageUtil.resizeImage把正常大小的图片，改变大小之后，分别复制到productSingle_middle和productSingle_small目录下。
            if (ProductImageService.type_single.equals(pi.getType())) {
                File f_small = new File(imageFolder_small, fileName);
                File f_middle = new File(imageFolder_middle, fileName);

                //ImageUtil.resizeImage可以把图片转换大小并复制到文件夹下
                ImageUtil.resizeImage(f, 56, 56, f_small);
                ImageUtil.resizeImage(f, 217, 190, f_middle);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //处理完毕之后，客户端条跳转到admin_productImage_list?pid=，并带上pid。
        return "redirect:admin_productImage_list?pid=" + pi.getPid();
    }


    @RequestMapping("admin_productImage_delete")
    //获取id
    public String delete(int id, HttpSession session) {
        //根据id获取ProductImage 对象pi
        ProductImage pi = productImageService.get(id);

        String fileName = pi.getId() + ".jpg";
        String imageFolder;
        String imageFolder_small = null;
        String imageFolder_middle = null;

        //借助productImageService，删除数据
        //如果是单个图片，那么删除3张正常，中等，小号图片
        //如果是详情图片，那么删除一张图片
        if (ProductImageService.type_single.equals(pi.getType())) {
            imageFolder = session.getServletContext().getRealPath("img/productSingle");
            imageFolder_small = session.getServletContext().getRealPath("img/productSingle_small");
            imageFolder_middle = session.getServletContext().getRealPath("img/productSingle_middle");
            File imageFile = new File(imageFolder, fileName);
            File f_small = new File(imageFolder_small, fileName);
            File f_middle = new File(imageFolder_middle, fileName);
            imageFile.delete();
            f_small.delete();
            f_middle.delete();

        } else {
            imageFolder = session.getServletContext().getRealPath("img/productDetail");
            File imageFile = new File(imageFolder, fileName);
            imageFile.delete();
        }

        productImageService.delete(id);

        return "redirect:admin_productImage_list?pid=" + pi.getPid();
    }

    @RequestMapping("admin_productImage_list")
    //获取参数pid
    public String list(int pid, Model model) {
        //根据pid获取Product对象
        Product p = productService.get(pid);
        //根据pid对象获取单个图片的集合pisSingle
        List<ProductImage> pisSingle = productImageService.list(pid, ProductImageService.type_single);
        //根据pid对象获取详情图片的集合pisDetail
        List<ProductImage> pisDetail = productImageService.list(pid, ProductImageService.type_detail);

        //把product 对象，pisSingle ，pisDetail放在model上
        model.addAttribute("p", p);
        model.addAttribute("pisSingle", pisSingle);
        model.addAttribute("pisDetail", pisDetail);

        //服务端跳转到admin/listProductImage.jsp页面
        return "admin/listProductImage";
    }
}