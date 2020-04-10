package com.leyou.item.service;



import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.entity.Brand;
import com.leyou.item.mapper.BrandMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;


    /**
     * 查询品牌
     * @param key
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @return
     */
    public PageResult<BrandDTO> queryBrandListByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {

        PageHelper.startPage(page, rows);

        Example example = new Example(Brand.class);

        if (StringUtils.isNotBlank(key)){
            example.createCriteria()
                    .orLike("name","%"+key+"%")
                    .orEqualTo("letter",key);
        }

        example.setOrderByClause(sortBy+" "+(desc?"DESC":"ASC"));

        List<Brand> brandList = brandMapper.selectByExample(example);

        if (CollectionUtils.isEmpty(brandList)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }

        PageInfo<Brand> pageInfo = new PageInfo<>(brandList);
        long total = pageInfo.getTotal();
        int totalPages = pageInfo.getPages();
        List<Brand> brandPages = pageInfo.getList();//当前页数据

        PageResult<BrandDTO> pageResult = new PageResult<>(total, totalPages, BeanHelper.copyWithCollection(brandList, BrandDTO.class));

        return pageResult;
    }

    /**
     * 新增品牌信息
     * @param brandDTO
     * @param cids 品牌类型id
     * @return
     */
    @Transactional
    public void saveBrand(BrandDTO brandDTO, List<Long> cids) {

        Brand brand = BeanHelper.copyProperties(brandDTO, Brand.class);
        int count = brandMapper.insertSelective(brand);
        if (count!=1){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

        Long bId = brand.getId();
        count = brandMapper.insertCategoryBrand(bId,cids);
        if (count!=cids.size()){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    /**
     * 根据品牌id查询品牌
     * @param brandId
     */
    public BrandDTO queryBrandById(Long brandId) {
        Brand brand = brandMapper.selectByPrimaryKey(brandId);
        if (brand == null){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return BeanHelper.copyProperties(brand, BrandDTO.class);
    }

    /**
     * 根据分类id查询品牌信息
     * @param categoryId
     * @return
     */
    public List<BrandDTO> queryBrandListByCid(Long categoryId) {


        List<Brand> brandList = brandMapper.queryBrandListByCid(categoryId);
        if (CollectionUtils.isEmpty(brandList)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }

        return BeanHelper.copyWithCollection(brandList,BrandDTO.class);
    }

    /**
     * 根据品牌的id集合批量查询品牌列表
     * @param brandIds
     * @return
     */
    public List<BrandDTO> queryBrandByIds(List<Long> brandIds) {
        List<Brand> brandList = brandMapper.selectByIdList(brandIds);
        // 判空
        if(CollectionUtils.isEmpty(brandList)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        // 返回
        return BeanHelper.copyWithCollection(brandList, BrandDTO.class);
    }
}
