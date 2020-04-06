package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.entity.SpecGroup;
import com.leyou.item.mapper.SpecGroupMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class SpecService {

    @Autowired
    private SpecGroupMapper specGroupMapper;

    /**
     * 查询规格组
     * @param id
     * @return
     */
    public List<SpecGroupDTO> querySpecGroupByCid(Long id) {

        SpecGroup specGroup = new SpecGroup();
        specGroup.setCid(id);

        List<SpecGroup> specGroupList = specGroupMapper.select(specGroup);

        if (CollectionUtils.isEmpty(specGroupList)){
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }

        List<SpecGroupDTO> specGroupDTOS = BeanHelper.copyWithCollection(specGroupList, SpecGroupDTO.class);

        return specGroupDTOS;
    }
}
