package com.leyou.search.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * 根据条件搜索商品
     * @param searchRequest 搜索条件
     * @return
     */
    @PostMapping("/page")
    public ResponseEntity<PageResult<GoodsDTO>> search(@RequestBody SearchRequest searchRequest){

        PageResult<GoodsDTO> pageResult = searchService.search(searchRequest);
        return ResponseEntity.ok(pageResult);
    }


    /**
     * 根据条件查询过滤参数  比如分类，品牌
     * @param searchRequest 查询条件
     * @return  过滤项
     */
    @PostMapping("/filter")
    public ResponseEntity<Map<String, List<?>>> filter(@RequestBody SearchRequest searchRequest){

        Map<String, List<?>> filterMap = searchService.filter(searchRequest);
        return ResponseEntity.ok(filterMap);
    }
}
