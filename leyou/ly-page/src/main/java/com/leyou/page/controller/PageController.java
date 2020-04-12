package com.leyou.page.controller;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class PageController {

    @Autowired
    private PageService pageService;

    /**
     * 查询商品详情
     * @param spuId
     * @return 跳转到页面
     */
    @GetMapping("/item/{id}.html")
    public String toItemHtml(Model model,@PathVariable("id") Long spuId){

        Map<String,Object> data = pageService.loadItemData(spuId);
        model.addAllAttributes(data);
        return "item";
    }


    @GetMapping("/hello")
    public String hello(Model model){

        model.addAttribute("msg","666666666666");

        return "hello";
    }
}
