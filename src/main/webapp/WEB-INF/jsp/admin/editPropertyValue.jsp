<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" import="java.util.*"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@include file="../include/admin/adminHeader.jsp"%>
<%@include file="../include/admin/adminNavigator.jsp"%>

<title>编辑产品属性值</title>

<script>
    $(function() {
        //监听输入框上的keyup事件
        $("input.pvValue").keyup(function(){
            //获取输入框里的值
            var value = $(this).val();

            var page = "admin_propertyValue_update";
            //获取输入框上的自定义属性pvid，这就是当前PropertyValue对应的id
            var pvid = $(this).attr("pvid");
            var parentSpan = $(this).parent("span");
            //把边框的颜色修改为黄色，表示正在修改的意思
            parentSpan.css("border","1px solid yellow");
            //借助JQuery的ajax函数 $.post，把id和值，提交到admin_propertyValue_update
            $.post(
                //admin_propertyValue_update导致PropertyValueController的update方法被调用
                page,
                {"value":value,"id":pvid},
                function(result){
                    //浏览器判断如果返回值是"success",那么就把边框设置为绿色，表示修改成功，否则设置为红色，表示修改失败
                    if("success"==result)
                        parentSpan.css("border","1px solid green");
                    else
                        parentSpan.css("border","1px solid red");
                }
            );
        });
    });
</script>

<div class="workingArea">
    <ol class="breadcrumb">
        <li><a href="admin_category_list">所有分类</a></li>
        <li><a href="admin_product_list?cid=${p.category.id}">${p.category.name}</a></li>
        <li class="active">${p.name}</li>
        <li class="active">编辑产品属性</li>
    </ol>

    <div class="editPVDiv">
        <c:forEach items="${pvs}" var="pv">
            <div class="eachPV">
                <span class="pvName" >${pv.property.name}</span>
                <span class="pvValue"><input class="pvValue" pvid="${pv.id}" type="text" value="${pv.value}"></span>
            </div>
        </c:forEach>
        <div style="clear:both"></div>
    </div>

</div>