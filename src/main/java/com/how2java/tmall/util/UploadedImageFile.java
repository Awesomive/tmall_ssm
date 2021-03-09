package com.how2java.tmall.util;

import org.springframework.web.multipart.MultipartFile;

public class UploadedImageFile {
/*
这里的属性名称image必须和 listCtegory.jsp 中上传部分的type="file"的name值保持一致。

<input id="categoryPic" accept="image/*" type="file" name="image" />
*/
    MultipartFile image;

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }

}