package com.leyou.item.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/brand")
public class BrandController {

    @Autowired
    public BrandService brandService;

    /**
     * 查询品牌
     * @param key
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @return
     */
    @GetMapping("page")
    public ResponseEntity<PageResult<BrandDTO>> queryBrandListByPage(
            @RequestParam(value="key", required=false) String key,
            @RequestParam(value="page", defaultValue = "1") Integer page,
            @RequestParam(value="rows", defaultValue = "10") Integer rows,
            @RequestParam(value="sortBy") String sortBy,
            @RequestParam(value="desc",defaultValue = "false") Boolean desc
    ){

        PageResult<BrandDTO> pageResult =  brandService.queryBrandListByPage(key,page,rows,sortBy,desc);
        return ResponseEntity.ok().body(pageResult);
    }

    /**
     * 新增品牌信息
     * @param brandDTO
     * @param cids 品牌类型id
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> saveBrand(BrandDTO brandDTO, @RequestParam("cids")List<Long> cids){

        brandService.saveBrand(brandDTO, cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();

    }

    /**
     * 根据分类id查询品牌信息
     * @param categoryId
     * @return
     */
    @GetMapping("/of/category")
    public ResponseEntity<List<BrandDTO>> queryBrandListByCid(@RequestParam("id")Long categoryId){

        List<BrandDTO> brands =  brandService.queryBrandListByCid(categoryId);
        return ResponseEntity.ok(brands);
    }


    /**
     * 通过品牌id查询品牌信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ResponseEntity<BrandDTO> queryBrandById(@PathVariable("id") Long id){

        BrandDTO brandDTO = brandService.queryBrandById(id);
        return ResponseEntity.ok(brandDTO);
    }

    /**
     * 根据品牌的id集合批量查询品牌列表
     * @param brandIds
     * @return
     */
    @GetMapping("/list")
    public ResponseEntity<List<BrandDTO>> queryBrandByIds(@RequestParam("ids") List<Long> brandIds){
        List<BrandDTO> brandDTOList = brandService.queryBrandByIds(brandIds);
        return ResponseEntity.ok(brandDTOList);
    }
}
