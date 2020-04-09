package com.leyou.search.dao;

import com.leyou.search.bo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;

public interface GoodsRepository extends ElasticsearchCrudRepository<Goods,Long> {
}
