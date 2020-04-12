package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.entity.SpecGroup;
import com.leyou.item.entity.SpecParam;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SpecService {

    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;

    /**
     * 查询分类Id规格组
     * @param id 分类Id
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


    /**
     * 查询规格参数
     * @param gid
     * @param cid
     * @param searching
     * @return
     */
    public List<SpecParamDTO> querySpecParam(Long gid, Long cid, Boolean searching) {

        if (gid == null && cid == null){
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }

        SpecParam specParam = new SpecParam();
        specParam.setCid(cid);
        specParam.setGroupId(gid);
        specParam.setSearching(searching);

        List<SpecParam> specParamList = specParamMapper.select(specParam);
        if (CollectionUtils.isEmpty(specParamList)){
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }

        return BeanHelper.copyWithCollection(specParamList,SpecParamDTO.class);
    }

    /**
     * 根据分类id查询规格组和组内参数
     * @param categoryId
     * @return
     */
    public List<SpecGroupDTO> queryCategoryAndParamsByCategoryId(Long categoryId) {

        //1.查询规格组
        List<SpecGroupDTO> specGroupDTOList = querySpecGroupByCid(categoryId);

        //2.查询组内参数
        List<SpecParamDTO> params = querySpecParam(null, categoryId, null);
        Map<Long, List<SpecParamDTO>> paramMap = params.stream().collect(Collectors.groupingBy(SpecParamDTO::getGroupId));
        for (SpecGroupDTO specGroupDTO : specGroupDTOList) {
            specGroupDTO.setParams(paramMap.get(specGroupDTO.getId()));
        }

        //3.返回规格组
        return specGroupDTOList;
    }
}
