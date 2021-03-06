package com.leyou.item.client;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@FeignClient(value = "item-service")
public interface ItemClient {

    /**
     * 分页查询spu商品
     * @param key
     * @param page
     * @param rows
     * @param saleable
     * @return
     */
    @GetMapping("/spu/page")
    public PageResult<SpuDTO> querySpuByPage(
            @RequestParam(value="key", required=false) String key,
            @RequestParam(value="page", defaultValue = "1",required=false) Integer page,
            @RequestParam(value="rows", defaultValue = "5",required=false) Integer rows,
            @RequestParam(value="saleable",required=false) Boolean saleable
    );


    /**
     * 根据spu的id查询sku集合
     * @param spuId     spu的id
     * @return          sku的：列表
     */
    @GetMapping("/sku/of/spu")
    public List<SkuDTO> querySkuListBySpuId(@RequestParam("id") Long spuId);


    /**
     * 根据spuId查询spuDetail
     * @param spuId
     * @return
     */
    @GetMapping("/spu/detail")
    public SpuDetailDTO querySpuDetailBySpuId(@RequestParam("id") Long spuId );


    /**
     * 根据id集合，查询分类信息
     * @param ids
     * @return
     */
    @GetMapping("/category/list")
    public List<CategoryDTO> queryCategoryByIds(@RequestParam("ids")List<Long> ids);


    /**
     * 通过品牌id查询品牌信息
     * @param id
     * @return
     */
    @GetMapping("/brand/{id}")
    public BrandDTO queryBrandById(@PathVariable("id") Long id);


    /**
     * 查询规格参数
     * @param gid 主id
     * @param cid 分类id
     * @param searching 是否搜索
     * @return
     */
    @GetMapping("/spec/params")
    public List<SpecParamDTO> querySpecParam(
            @RequestParam(value = "gid",required = false)Long gid,
            @RequestParam(value = "cid",required = false)Long cid,
            @RequestParam(value = "searching",required = false)boolean searching
    );

    /**
     * 批量查询品牌
     * @param brandIds
     * @return
     */
    @GetMapping("/brand/list")
    public List<BrandDTO> queryBrandByIds(@RequestParam("ids") List<Long> brandIds);

    /**
     * 根据spuId查询spu
     * @param id spu的Id
     * @return
     */
    @GetMapping("/spu/{id}")
    public SpuDTO querySpuById(@PathVariable("id") Long id);

    /**
     * 根据分类id查询规格组和组内参数
     * @param categoryId
     * @return
     */
    @GetMapping("/spec/of/category")
    public List<SpecGroupDTO> queryCategoryAndParamsByCategoryId(@RequestParam("id") Long categoryId);
}
