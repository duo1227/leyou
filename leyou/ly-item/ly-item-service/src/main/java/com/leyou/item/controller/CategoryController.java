package com.leyou.item.controller;

import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 根据父id查询分类信息
     * @param pid
     * @return
     */
    @GetMapping("/category/of/parent")
    public ResponseEntity<List<CategoryDTO>> queryCategoryByParentId(@RequestParam("pid")Long pid){

        List<CategoryDTO> categoryList = categoryService.findCategoryByParentId(pid);

        //return ResponseEntity.status(HttpStatus.OK).body(categoryList);
        return ResponseEntity.ok(categoryList);

    }


    /**
     * 根据id集合，查询分类信息
     * @param ids
     * @return
     */
    @GetMapping("/category/list")
    public ResponseEntity<List<CategoryDTO>> queryCategoryByIds(@RequestParam("ids")List<Long> ids){

        List<CategoryDTO> categoryList = categoryService.queryCategoryByIds(ids);

        //return ResponseEntity.status(HttpStatus.OK).body(categoryList);
        return ResponseEntity.ok(categoryList);

    }
}
