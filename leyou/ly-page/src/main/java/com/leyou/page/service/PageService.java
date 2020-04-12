package com.leyou.page.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpuDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Value("${ly.static.itemDir}")
    private String itemDir;

    @Value("${ly.static.templateName}")
    private String templateName;

    /**
     * 生成静态化页面
     */
    public void createItemHtml(Long spuId){
        // 1、准备上下文对象
        Context context = new Context();
        // 1.1 根据spuid查询出商品详情所有的数据
        context.setVariables(loadItemData(spuId));
        // 2、生成目录
        File dir = new File(itemDir);
        if(!dir.exists()){
            if(!dir.mkdir()){
                throw new LyException(ExceptionEnum.DIRECTORY_WRITER_ERROR);
            }
        }
        // 3、创建文件
        File file = new File(dir, spuId + ".html");
        // 4、生成静态化页面
        try(PrintWriter writer = new PrintWriter(file,"UTF-8")){
            templateEngine.process(templateName,context, writer);
        }catch (Exception e){
            log.error("【静态页服务】生成静态页失败：{}",e.getMessage());
            throw new LyException(ExceptionEnum.FILE_WRITER_ERROR);
        }
    }
}
