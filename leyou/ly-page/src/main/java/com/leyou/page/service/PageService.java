package com.leyou.page.service;

import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpuDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageService {

    @Autowired
    private ItemClient itemClient;

    public Map<String, Object> loadItemData(Long spuId) {

        //查询sku
        SpuDTO spuDTO = itemClient.querySpuById(spuId);

        //查询分类信息
        List<CategoryDTO> categoryDTOS = itemClient.queryCategoryByIds(spuDTO.getCategoryIds());

        //查询品牌信息
        BrandDTO brandDTO = itemClient.queryBrandById(spuDTO.getBrandId());

        //查询规格参数
        List<SpecGroupDTO> specGroupList = itemClient.queryCategoryAndParamsByCategoryId(spuDTO.getCid3());

        Map<String, Object> data = new HashMap<>();
        data.put("categories", categoryDTOS); // 三级分类的对象集合
        data.put("brand", brandDTO);      //品牌对象
        data.put("spuName", spuDTO.getName());    // 商品名称来自于SpuDTO
        data.put("subTitle", spuDTO.getSubTitle());   //商品标题来自于SpuDTO
        data.put("skus", spuDTO.getSkus());       //sku集合
        data.put("detail", spuDTO.getSpuDetail());     //SpuDetail对象
        data.put("specs", specGroupList);      // 规格组集合，每个规格组中有个规格参数的集合

        return data;
    }
}
