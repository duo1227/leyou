package com.leyou.search.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.*;
import com.leyou.search.bo.Goods;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class SearchService {

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;//ES原生方法

    public Goods buildGoods(SpuDTO spuDTO){
        //spu的ID
        Long spuId = spuDTO.getId();

        // 1、查询sku的集合
        List<SkuDTO> skuDTOList = itemClient.querySkuListBySpuId(spuId);
        List<Map<String, Object>> skuList = new ArrayList<>();
        for (SkuDTO skuDTO : skuDTOList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", skuDTO.getId());
            map.put("image", StringUtils.substringBefore(skuDTO.getImages(), ","));
            map.put("price", skuDTO.getPrice());
            map.put("title", skuDTO.getTitle());
            skuList.add(map);
        }
        // 1.1 把对象转成json字符串
        String skuJsonStr = JsonUtils.toString(skuList);

        // 2、sku的价格集合: set有去重的功能，用于在页面搜索过滤
        Set<Long> priceSet = skuDTOList.stream().map(SkuDTO::getPrice).collect(Collectors.toSet());

        // 3、搜索字段：包含商品名称+分类名称+品牌名称
        // 3.1 分类名称
        List<CategoryDTO> categoryDTOList = itemClient.queryCategoryByIds(spuDTO.getCategoryIds());
        String categoryNames = categoryDTOList.stream().map(CategoryDTO::getName).collect(Collectors.joining());
        // 3.2 品牌名称
        BrandDTO brandDTO = itemClient.queryBrandById(spuDTO.getBrandId());
        String all = spuDTO.getName()+ categoryNames + brandDTO.getName();

        // 4、规格参数：Map结构
        // 4.1 准备一个map接收规格参数
        Map<String, Object> specs = new HashMap<>();
        // 4.2 查询出用于搜索过滤的规格参数
        List<SpecParamDTO> specParamDTOS = itemClient.querySpecParam(null, spuDTO.getCid3(), true);
        // 4.3 查询出商品详情
        SpuDetailDTO spuDetailDTO = itemClient.querySpuDetailBySpuId(spuId);
        // 4.4 通用规格参数
        String genericSpec = spuDetailDTO.getGenericSpec();
        Map<Long, Object> genericSpecMap = JsonUtils.toMap(genericSpec, Long.class, Object.class);
        // 4.5 特有规格参数
        String specialSpec = spuDetailDTO.getSpecialSpec();
        Map<Long, List<String>> specialSpecMap = JsonUtils.nativeRead(specialSpec, new TypeReference<Map<Long, List<String>>>() {
        });
        // 4.6 迭代规格参数
        for (SpecParamDTO param : specParamDTOS) {
            String key = param.getName();
            Object value = null;
            if(param.getGeneric()){
                // 通用规格参数
                value = genericSpecMap.get(param.getId());
            }else{
                // 特有规格参数
                value = specialSpecMap.get(param.getId());
            }
            // 如果是数字，我们转成字符串的片段
            if(param.getNumeric()){
                value = chooseSegment(value, param);
            }
            specs.put(key, value);
        }

        // 5、实例化一个goods对象
        Goods goods = new Goods();
        goods.setId(spuId);
        goods.setSubTitle(spuDTO.getSubTitle());
        goods.setSkus(skuJsonStr); // 去查询skus列表转成json再设置进去
        goods.setPrice(priceSet); // 所有sku的价格集合
        goods.setAll(all); // all字段，这个字段用于搜索，包含商品名称+分类名称+品牌名称
        goods.setSpecs(specs);// 对应的所有的规格参数，用于过滤
        goods.setBrandId(spuDTO.getBrandId());
        goods.setCategoryId(spuDTO.getCid3());
        goods.setCreateTime(spuDTO.getCreateTime().getTime());

        // 6、返回
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

    /**
     *  根据条件搜索商品
     * @param searchRequest     搜索条件
     * @return
     */
    public PageResult<GoodsDTO> search(SearchRequest searchRequest) {
        // 如果没有查询条件，我们抛异常
        String key = searchRequest.getKey();
        if(StringUtils.isBlank(key)){
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }

        // 原生的查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        // 1、控制字段的数量
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"}, null));

        // 2、拼接搜索条件:  分词查询
        queryBuilder.withQuery(builderSearchKey(searchRequest));

        // 3、分页条件设置
        Integer page = searchRequest.getPage() - 1;  // springdata的起始页是从0开始，所以减一
        Integer size = searchRequest.getSize();      // 页大小
        queryBuilder.withPageable(PageRequest.of(page, size));

        // 执行查询
        AggregatedPage<Goods> goodsPage = elasticsearchTemplate.queryForPage(queryBuilder.build(), Goods.class);

        // 获取结果
        long totalElements = goodsPage.getTotalElements(); // 总记录数
        int totalPages = goodsPage.getTotalPages();     // 总页数
        List<Goods> goodsList = goodsPage.getContent(); //当前页数据

        // 返回数据
        return new PageResult<>(
                totalElements,
                totalPages,
                BeanHelper.copyWithCollection(goodsList, GoodsDTO.class));
    }

    /**
     * 根据条件查询过滤参数
     *      1）分类
     *      2）品牌
     *      3）过滤参数
     *
     * @param searchRequest     查询条件
     * @return                  所有的过滤项
     */
    public Map<String, List<?>> filter(SearchRequest searchRequest) {
        // 如果没有查询条件，我们抛异常
        String key = searchRequest.getKey();
        if(StringUtils.isBlank(key)){
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }

        // 实例化一个map来装过滤项
        Map<String, List<?>> filterMap = new LinkedHashMap<>();
        // 原生的查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        // 1、控制字段的数量
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{""}, null));
        // 2、拼接搜索条件:  分词查询
        queryBuilder.withQuery(builderSearchKey(searchRequest));
        // 3、分页条件设置
        queryBuilder.withPageable(PageRequest.of(0, 1));

        // 4、添加聚合条件
        // 4.1 聚合分类
        String categoryAgg = "categoryAgg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAgg).field("categoryId").size(1000));
        // 4.2 聚合品牌
        String brandAgg = "brandAgg";
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAgg).field("brandId").size(1000));
        // 执行查询操作
        AggregatedPage<Goods> goodsPage = elasticsearchTemplate.queryForPage(queryBuilder.build(), Goods.class);

        // 获取结果数据
        Aggregations agg = goodsPage.getAggregations();
        // 获取分类
        Terms categoryTerm = agg.get(categoryAgg);
        // 把分类聚合结果放入filterMap
        List<Long> categoryIds = handlerCategoryFilter(categoryTerm, filterMap);
        // 获取品牌
        Terms brandTerm = agg.get(brandAgg);
        handlerBrandFilter(brandTerm, filterMap);


        // 添加过滤参数
        if(categoryIds!=null && categoryIds.size() == 1){
            addSpecParamFilter(categoryIds.get(0), builderSearchKey(searchRequest), filterMap);
        }
        // 返回数据
        return filterMap;
    }

    /**
     * 查询规格过滤参数
     * @param categoryId           分类的id
     * @param builderSearchKey      聚合条件
     * @param filterMap             我们把所有的规格参数值放入map
     */
    private void addSpecParamFilter(Long categoryId, QueryBuilder builderSearchKey, Map<String, List<?>> filterMap) {
        // 2、通过分类的id去查询规格过滤参数：
        List<SpecParamDTO> specParamDTOS = itemClient.querySpecParam(null, categoryId, true);
        // 3、我们创建es的原生查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{""}, null));
        queryBuilder.withQuery(builderSearchKey);
        queryBuilder.withPageable(PageRequest.of(0, 1));
        // 4、拼接聚合的fidld的名称
        for (SpecParamDTO specParm : specParamDTOS) {
            String aggName = specParm.getName();
            String fieldName = "specs."+aggName;
            queryBuilder.addAggregation(AggregationBuilders.terms(aggName).field(fieldName).size(1000));
        }
        // 5、发起聚合查询
        AggregatedPage<Goods> goodsPage = elasticsearchTemplate.queryForPage(queryBuilder.build(), Goods.class);
        // 6、迭代获取结果：获取桶中的数据
        Aggregations aggregations = goodsPage.getAggregations();
        // 7、迭代获取桶中的内容
        for (SpecParamDTO specParm : specParamDTOS) {
            String aggName = specParm.getName();
            Terms terms = aggregations.get(aggName);
            List<String> specList = terms.getBuckets().stream()
                    .map(Terms.Bucket::getKeyAsString)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
            filterMap.put(aggName, specList);
        }
    }


    /**
     * 把分类的聚合结果放入filterMap中
     * @param categoryTerm
     * @param filterMap
     * @return 分类的id集合，用于查询规格过滤参数名称
     */
    private List<Long> handlerCategoryFilter(Terms categoryTerm, Map<String, List<?>> filterMap) {
        // 得到聚合的所有的桶
        List<? extends Terms.Bucket> buckets = categoryTerm.getBuckets();
        // 收集桶中的所有的key，他们其实就是分类的id
        List<Long> categoryIds = buckets.stream().map(Terms.Bucket::getKeyAsNumber).map(Number::longValue).collect(Collectors.toList());
        // 去数据库中查询出分类的信息
        List<CategoryDTO> categoryDTOList = null;
        if(!CollectionUtils.isEmpty(categoryIds)){
            categoryDTOList = itemClient.queryCategoryByIds(categoryIds);
        }
        // 存入filterMap
        filterMap.put("分类", categoryDTOList);
        return categoryIds;
    }


    /**
     * 把品牌的聚合结果放入filterMap中
     * @param brandTerm
     * @param filterMap
     */
    private void handlerBrandFilter(Terms brandTerm, Map<String, List<?>> filterMap) {
        // 收集桶中所有的品牌的id
        List<Long> brandIds = brandTerm.getBuckets().stream()
                .map(Terms.Bucket::getKeyAsNumber)
                .map(Number::longValue)
                .collect(Collectors.toList());
        // 根据品牌id集合查询品牌信息
        List<BrandDTO> brandDTOList = null;
        if(!CollectionUtils.isEmpty(brandIds)) {
            brandDTOList = itemClient.queryBrandByIds(brandIds);
        }
        // 放入filtermap
        filterMap.put("品牌",brandDTOList);
    }


    /**
     * 抽取查询条件的拼接
     * @param searchRequest
     * @return
     */
    private QueryBuilder builderSearchKey(SearchRequest searchRequest){
        // 组合条件查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 1、查询条件拼接
        boolQueryBuilder.must(QueryBuilders.matchQuery("all", searchRequest.getKey()).operator(Operator.AND));
        // 2、拼接过滤参数
        Map<String, Object> filters = searchRequest.getFilters();
        if(!CollectionUtils.isEmpty(filters)) {
            for (Map.Entry<String, Object> entry : filters.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                String fieldName = "";
                if ("分类".equals(key)) {
                    fieldName = "categoryId";
                } else if ("品牌".equals(key)) {
                    fieldName = "brandId";
                } else {
                    fieldName = "specs." + key;
                }
                boolQueryBuilder.filter(QueryBuilders.termsQuery(fieldName, value));
            }
        }
        return boolQueryBuilder;
    }
}