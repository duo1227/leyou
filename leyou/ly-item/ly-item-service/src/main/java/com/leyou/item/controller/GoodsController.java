package com.leyou.item.controller;


import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.SkuDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.dto.SpuDetailDTO;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    /**
     * 分页查询spu商品
     * @param key
     * @param page
     * @param rows
     * @param saleable
     * @return
     */
    @GetMapping("/spu/page")
    public ResponseEntity<PageResult<SpuDTO>> querySpuByPage(
            @RequestParam(value="key", required=false) String key,
            @RequestParam(value="page", defaultValue = "1",required=false) Integer page,
            @RequestParam(value="rows", defaultValue = "5",required=false) Integer rows,
            @RequestParam(value="saleable",required=false) Boolean saleable
    ){

        PageResult<SpuDTO> pageResult = goodsService.querySpuByPage(key,page,rows,saleable);
        return ResponseEntity.ok(pageResult);
    }


    /**
     * 新增商品
     * @param spuDTO
     * @return
     */
    @PostMapping("/goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuDTO spuDTO){

        goodsService.saveGoods(spuDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    /**
     * 商品上下架
     * @param spuId
     * @param saleable
     * @return
     */
    @PutMapping("/spu/saleable")
    public ResponseEntity<Void> updateSaleable(
            @RequestParam("id")Long spuId,
            @RequestParam("saleable")Boolean saleable
    ){

        goodsService.updateSaleable(spuId, saleable);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }


    /**
     * 根据spuId查询spuDetail
     * @param spuId
     * @return
     */
    @GetMapping("/spu/detail")
    public ResponseEntity<SpuDetailDTO> querySpuDetailBySpuId(@RequestParam("id") Long spuId ){
        SpuDetailDTO spuDetailDTO = goodsService.querySpuDetailBySpuId(spuId);
        return ResponseEntity.ok(spuDetailDTO);
    }




    /**
     * 根据spu的id查询sku集合
     * @param spuId     spu的id
     * @return          sku的：列表
     */
    @GetMapping("/sku/of/spu")
    public ResponseEntity<List<SkuDTO>> querySkuListBySpuId(@RequestParam("id") Long spuId){
        List<SkuDTO> skuList = goodsService.querySkuListBySpuId(spuId);
        return ResponseEntity.ok(skuList);
    }

    /**
     * 修改商品
     * @param spuDTO
     * @return
     */
    @PutMapping("/goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuDTO spuDTO){

        goodsService.updateGoods(spuDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }
}
