package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.entity.Category;
import com.leyou.item.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;


    public List<CategoryDTO> findCategoryByParentId(Long pid) {

        Category record = new Category();
        record.setParentId(pid);
        List<Category> categoryList = categoryMapper.select(record);
        if (CollectionUtils.isEmpty(categoryList)){
            throw  new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        List<CategoryDTO> categoryDTOS = BeanHelper.copyWithCollection(categoryList, CategoryDTO.class);
        return categoryDTOS;
    }

    /**
     * 根据cid集合查询分类
     * @param categoryIds
     * @return
     */
    public List<CategoryDTO> queryCategoryByIds(List<Long> categoryIds) {
        List<Category> categories = categoryMapper.selectByIdList(categoryIds);
        return BeanHelper.copyWithCollection(categories,CategoryDTO.class);
    }
}
