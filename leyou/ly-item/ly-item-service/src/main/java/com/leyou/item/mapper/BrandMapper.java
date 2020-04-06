package com.leyou.item.mapper;

import com.leyou.item.entity.Brand;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand> {

    /**
     * 插入品牌分类中间表
     * @param bId 品牌id
     * @param cids 分类id集合
     * @return 返回插入的条数
     */
    int insertCategoryBrand(@Param("bId") Long bId, @Param("cid") List<Long> cids);


    /**
     * 根据分类id查询品牌信息
     * @param categoryId
     * @return
     */
    @Select("SELECT b.* FROM tb_category_brand c INNER JOIN tb_brand b ON c.`brand_id` = b.`id` WHERE c.`category_id` = #{categoryId};")
    List<Brand> queryBrandListByCid(Long categoryId);
}
