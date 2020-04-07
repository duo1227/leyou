package com.leyou.search.service;


import com.leyou.common.utils.JsonUtils;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.SkuDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.search.bo.Goods;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SearchService {

    @Autowired
    private ItemClient itemClient;

    public Goods buildGoods(SpuDTO spuDTO){

        //获取spuId
        Long spuId = spuDTO.getId();

        //1、spu下所有的sku
        List<SkuDTO> skuDTOS = itemClient.querySkuListBySpuId(spuId);
        List<Map<String,Object>> skus = new ArrayList<>();
        for (SkuDTO skuDTO : skuDTOS) {
            Map<String,Object> skuMap = new HashMap<>();
            skuMap.put("id",skuDTO.getId());
            skuMap.put("image", StringUtils.substringBefore(skuDTO.getImages(),","));
            skuMap.put("price",skuDTO.getPrice());
            skuMap.put("title",skuDTO.getTitle());
            skus.add(skuMap);
        }
        String skuJsons = JsonUtils.toString(skus);

        //2、sku的价格集合
        Set<Long> skuSet = skuDTOS.stream().map(SkuDTO::getPrice).collect(Collectors.toSet());


        //3、搜索字段，包含名称、分类、品牌、等

        //4、规格参数，包含key和value

        //实例Goods对象
        Goods goods = new Goods();
        goods.setId(spuId);
        goods.setSubTitle(spuDTO.getSubTitle());
        goods.setSkus(skuJsons); //去查询sku列表，再转换json
        goods.setAll(null); //TODO 所有需要被搜索的信息
        goods.setPrice(null);//TODO 所有sku价格集合
        goods.setSpecs(null);//TODO 对应所以的规格参数，用于过滤
        goods.setBrandId(spuDTO.getBrandId());
        goods.setCategoryId(spuDTO.getCid3());
        goods.setCreateTime(spuDTO.getCreateTime().getTime());


        return goods;
    }
}
