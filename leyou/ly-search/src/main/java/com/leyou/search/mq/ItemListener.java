package com.leyou.search.mq;

import com.leyou.search.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.leyou.common.constants.MQConstants.Exchange.*;
import static com.leyou.common.constants.MQConstants.Queue.*;
import static com.leyou.common.constants.MQConstants.RoutingKey.*;


/**
 * 消费者：
 *      监听mq
 */
@Slf4j
@Component
public class ItemListener {

    @Autowired
    private SearchService searchService;

    /**
     * 当商品上架的时候，我们往ES中插入索引
     *
     * @param spuId
     */
    @RabbitListener( //@RabbitListener监听rabbitmq
            bindings = @QueueBinding(
                    value = @Queue( // 配置队列
                            value=SEARCH_ITEM_UP, // 队列名称
                            durable = "true" // 是否持久化
                    ),
                    exchange = @Exchange(
                            value=ITEM_EXCHANGE_NAME,//交换机名称
                            type = ExchangeTypes.TOPIC , //采用主题类型
                            ignoreDeclarationExceptions = "true"// 创建交换机失败的时候，继续创建其他属性
                    ),
                    key = ITEM_UP_KEY
            )
    )

    public void itemUp(Long spuId){
        if(spuId != null){
            log.info("【搜索微服务】商品上架，往ES索引库中插入索引，商品ID = {}",spuId);
            searchService.saveGoods(spuId);
        }
    }


    /**
     * 当商品下架的时候，我们删除ES的索引
     * @param spuId
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue( // 配置队列
                            value=SEARCH_ITEM_DOWN, // 队列名称
                            durable = "true" // 是否持久化
                    ),
                    exchange = @Exchange(
                            value=ITEM_EXCHANGE_NAME,//交换机名称
                            type = ExchangeTypes.TOPIC , //采用主题类型
                            ignoreDeclarationExceptions = "true"// 创建交换机失败的时候，继续创建其他属性
                    ),
                    key = ITEM_DOWN_KEY
            )
    )
    public void itemDown(Long spuId){
        if(spuId != null){
            log.info("【搜索微服务】商品下架，ES索引库删除索引，商品ID = {}",spuId);
            searchService.deleteGoods(spuId);
        }
    }
}
