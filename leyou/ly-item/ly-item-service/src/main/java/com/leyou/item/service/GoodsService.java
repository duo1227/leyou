package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.*;
import com.leyou.item.entity.Sku;
import com.leyou.item.entity.Spu;
import com.leyou.item.entity.SpuDetail;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

import static com.leyou.common.constants.MQConstants.Exchange.ITEM_EXCHANGE_NAME;
import static com.leyou.common.constants.MQConstants.RoutingKey.ITEM_DOWN_KEY;
import static com.leyou.common.constants.MQConstants.RoutingKey.ITEM_UP_KEY;

@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private BrandService brandService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private AmqpTemplate amqpTemplate; //发消息


    /**
     * 分页查询商品列表
     * @param key
     * @param page
     * @param rows
     * @param saleable
     * @return
     */
    public PageResult<SpuDTO> querySpuByPage(String key, Integer page, Integer rows, Boolean saleable) {

        PageHelper.startPage(page,rows);

        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();

        //判断过滤条件
        if (StringUtils.isNotBlank(key)){
            criteria.andLike("name","%"+ key +"%");
        }

        //判断是否上架
        if (saleable != null){
            criteria.andEqualTo("saleable",saleable);
        }

        example.setOrderByClause("update_time DESC");

        List<Spu> spuList = spuMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(spuList)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }

        PageInfo<Spu> pageInfo = new PageInfo<>(spuList);
        List<SpuDTO> spuDTOS = BeanHelper.copyWithCollection(spuList, SpuDTO.class);

        //处理分类名称和品牌名称
        handlerCategoryAndBrandName(spuDTOS);

        return new PageResult<SpuDTO>(pageInfo.getTotal(),pageInfo.getPages(),spuDTOS);
    }

    /**
     * 给spuDTO添加品牌名称，和分类名称
     * @param spuDTOS
     */
    private void handlerCategoryAndBrandName(List<SpuDTO> spuDTOS) {

        //处理品牌
        for (SpuDTO spuDTO : spuDTOS) {
            Long brandId = spuDTO.getBrandId();
            BrandDTO brandDTO = brandService.queryBrandById(brandId);
            spuDTO.setBrandName(brandDTO.getName());

            //处理分类
            String cName = "";
            List<CategoryDTO> categoryDTOS = categoryService.queryCategoryByIds(spuDTO.getCategoryIds());
            for (CategoryDTO c : categoryDTOS) {
                cName += c.getName() + "/";
            }
            cName.substring(0, cName.length() - 2);
            spuDTO.setCategoryName(cName);
        }

    }


    /**
     * 添加商品
     * @param spuDTO
     */
    @Transactional
    public void saveGoods(SpuDTO spuDTO) {

        //添加Spu
        Spu spu = BeanHelper.copyProperties(spuDTO, Spu.class);
        spu.setSaleable(false);//新保存的商品都是下架操作
        int count = spuMapper.insert(spu);
        if (count != 1){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

        //添加spuDetail
        SpuDetailDTO spuDetailDTO = spuDTO.getSpuDetail();
        SpuDetail spuDetail = BeanHelper.copyProperties(spuDetailDTO, SpuDetail.class);
        spuDetail.setSpuId(spu.getId());
        count = spuDetailMapper.insert(spuDetail);
        if (count != 1){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

        //添加sku
        List<SkuDTO> skuDTOS = spuDTO.getSkus();
        List<Sku> skus = BeanHelper.copyWithCollection(skuDTOS, Sku.class);
        for (Sku sku : skus) {
            sku.setSpuId(spu.getId());//给sku表，加上SpuId数据
        }
        count = skuMapper.insertList(skus);
        if (count != skus.size()){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    /**
     * 商品上下架
     * @param spuId
     * @param saleable
     */
    public void updateSaleable(Long spuId, Boolean saleable) {

        //更新spu表的saleable
        Spu spu = new Spu();
        spu.setId(spuId);
        spu.setSaleable(saleable);
        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count != 1){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }

        //更新sku表的enable
        Sku sku = new Sku();
        sku.setEnable(saleable);

        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria().andEqualTo("spuId",spuId);

        //skuMapper.updateByExample(sku,example);
        count = skuMapper.updateByExampleSelective(sku, example);
        if (count < 1){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }

        // 3、上下架的操作，我们需要发送消息到队列中： 静态化服务和搜索微服务消费
        amqpTemplate.convertAndSend(
                ITEM_EXCHANGE_NAME,//交换机名字
                (saleable ? ITEM_UP_KEY:ITEM_DOWN_KEY),//判断是否上下架，给routing-key
                spuId //发送的数据
                );

    }

    /**
     * 根据spuId查询spuDetail
     * @param spuId
     * @return
     */
    public SpuDetailDTO querySpuDetailBySpuId(Long spuId) {

        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(spuId);
        if (spuDetail == null){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }

        return BeanHelper.copyProperties(spuDetail,SpuDetailDTO.class);
    }



    /**
     * 根据spu的id查询sku集合
     * @param spuId
     * @return
     */
    public List<SkuDTO> querySkuListBySpuId(Long spuId) {
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skuList = skuMapper.select(sku);
        if(CollectionUtils.isEmpty(skuList)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(skuList, SkuDTO.class);
    }



    /**
     * 更新商品
     * @param spuDTO
     * @return
     */
    @Transactional
    public void updateGoods(SpuDTO spuDTO) {

        Long spuId = spuDTO.getId();
        if (spuId == null){
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }

        //修改Spu
        Spu spu = BeanHelper.copyProperties(spuDTO, Spu.class);
        spu.setSaleable(null);
        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count != 1){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }

        //修改spuDetail
        SpuDetailDTO spuDetailDTO = spuDTO.getSpuDetail();
        SpuDetail spuDetail = BeanHelper.copyProperties(spuDetailDTO, SpuDetail.class);
        spuDetail.setSpuId(spuId);
        count = spuDetailMapper.updateByPrimaryKeySelective(spuDetail);
        if (count != 1){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }

        //修改sku
        //1.先删除
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        int size = skuMapper.selectCount(sku);
        count = skuMapper.delete(sku);
        if (size != count){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }

        //2.再添加sku
        List<SkuDTO> skuDTOS = spuDTO.getSkus();
        List<Sku> skus = BeanHelper.copyWithCollection(skuDTOS, Sku.class);
        for (Sku skuDate : skus) {
            skuDate.setSpuId(spu.getId());//给sku表，加上SpuId数据
        }
        count = skuMapper.insertList(skus);
        if (count != skus.size()){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

    }

    /**
     * 根据spuId查询spu
     * @param id spu的Id
     * @return
     */
    public SpuDTO querySpuById(Long id) {

        //查询spu的信息
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        SpuDTO spuDTO = BeanHelper.copyProperties(spu, SpuDTO.class);

        //往spuDTO加入Detail信息
        spuDTO.setSpuDetail(querySpuDetailBySpuId(id));

        //往spuDTO加入sku信息
        spuDTO.setSkus(querySkuListBySpuId(id));

        return spuDTO;
    }
}
