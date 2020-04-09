package com.leyou.search.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.*;
import com.leyou.search.bo.Goods;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


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
        String skuJsons = JsonUtils.toString(skus);//用工具类转换成json

        //2、sku的价格集合
        Set<Long> skuPriceSet = skuDTOS.stream().map(SkuDTO::getPrice).collect(Collectors.toSet());


        //3、All搜索字段，包含商品名称、分类、品牌、等
        //3.1分类名称
        List<CategoryDTO> categoryDTOS = itemClient.queryCategoryByIds(spuDTO.getCategoryIds());
        String categoryNames = categoryDTOS.stream().map(CategoryDTO::getName).collect(Collectors.joining());

        //3.2品牌名称
        BrandDTO brandDTO = itemClient.queryBrandById(spuDTO.getBrandId());
        String all = spuDTO.getName() + categoryNames + brandDTO.getName();

        //4、规格参数，包含key和value
        Map<String,Object> Specs = new HashMap<>();

        //4.1查询规格参数的key：当前商品所属分类下的需要搜索的规格参数
        List<SpecParamDTO> specParamDTOS = itemClient.querySpecParamByGid(null, spuDTO.getCid3(), true);

        //4.2查出规格参数
        SpuDetailDTO spuDetailDTO = itemClient.querySpuDetailBySpuId(spuId);
        //4.3通用的规格参数,并转换为Map
        String genericSpec = spuDetailDTO.getGenericSpec();
        Map<Long, Object> genericSpecMap = JsonUtils.toMap(genericSpec, Long.class, Object.class);
        //4.4特有的规格参数,并转换为Map
        String specialSpec = spuDetailDTO.getSpecialSpec();
        Map<Long, List<String>> specialSpecMap = JsonUtils.nativeRead(specialSpec, new TypeReference<Map<Long, List<String>>>() {
        });

        for (SpecParamDTO param : specParamDTOS) {
            String paramName = param.getName();

            Object value = null;
            if (param.getGeneric()){
                value = genericSpecMap.get(param.getId());
            }else {
                value = specialSpecMap.get(param.getId());
            }
            //是数字，就转为字符串字段
            if (param.getNumeric()){
               value = chooseSegment(value, param);
            }

            Specs.put(paramName,value);
        }


        //实例Goods对象
        Goods goods = new Goods();
        goods.setId(spuId);
        goods.setSubTitle(spuDTO.getSubTitle());
        goods.setSkus(skuJsons); //去查询sku列表，再转换json
        goods.setAll(all); //所有需要被搜索的信息
        goods.setPrice(skuPriceSet);// 所有sku价格集合
        goods.setSpecs(Specs);//对应所以的规格参数，用于过滤
        goods.setBrandId(spuDTO.getBrandId());
        goods.setCategoryId(spuDTO.getCid3());
        goods.setCreateTime(spuDTO.getCreateTime().getTime());


        return goods;
    }


    private String chooseSegment(Object value, SpecParamDTO p) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return "其它";
        }
        double val = parseDouble(value.toString());
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = parseDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = parseDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    private double parseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return 0;
        }
    }


}
