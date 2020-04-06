package com.leyou.item.controller;

import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.service.SpecParamService;
import com.leyou.item.service.SpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/spec")
public class SpecGroupController {

    @Autowired
    private SpecService specService;
    @Autowired
    private SpecParamService specParamService;


    /**
     * 查询规格组
     * @param id
     * @return
     */
    @GetMapping("/groups/of/category")
    public ResponseEntity<List<SpecGroupDTO>> querySpecGroupByCid(@RequestParam("id")Long id){

        List<SpecGroupDTO> specGroupDTO = specService.querySpecGroupByCid(id);

        return ResponseEntity.ok(specGroupDTO);

    }


    /**
     * 根据分类查询规格参数
     * @param gid
     * @param cid
     * @param searching
     * @return
     */
    @GetMapping("/params")
    public ResponseEntity<List<SpecParamDTO>> querySpecParamByGid(
            @RequestParam(value = "gid",required = false)Long gid,
            @RequestParam(value = "cid",required = false)Long cid,
            @RequestParam(value = "searching",required = false)boolean searching
            ){

        List<SpecParamDTO> specParamDTO = specParamService.querySpecParamByGid(gid,cid,searching);

        return ResponseEntity.ok(specParamDTO);
    }

}
