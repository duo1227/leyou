package com.leyou.page.mq;

import com.leyou.page.service.PageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.leyou.common.constants.MQConstants.Exchange.ITEM_EXCHANGE_NAME;
import static com.leyou.common.constants.MQConstants.Queue.PAGE_ITEM_DOWN;
import static com.leyou.common.constants.MQConstants.Queue.PAGE_ITEM_UP;
import static com.leyou.common.constants.MQConstants.RoutingKey.ITEM_DOWN_KEY;
import static com.leyou.common.constants.MQConstants.RoutingKey.ITEM_UP_KEY;


/**
 * 消费者：
 *      监听mq
 */
@Slf4j
@Component
public class ItemListener {

    @Autowired
    private PageService pageService;

    /**
     * 当商品上架的时候，生成静态页
     *
     * @param spuId
     */
    @RabbitListener( //@RabbitListener监听rabbitmq
            bindings = @QueueBinding(
                    value = @Queue( // 配置队列
                            value=PAGE_ITEM_UP, // 队列名称
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
            log.info("【静态页微服务】商品上架，往本地生成静态页，商品ID = {}",spuId);
            pageService.createItemHtml(spuId);
        }
    }


    /**
     * 当商品下架的时候，删除静态页
     * @param spuId
     */
    @RabbitListener( //@RabbitListener监听rabbitmq
            bindings = @QueueBinding(
                    value = @Queue( // 配置队列
                            value=PAGE_ITEM_DOWN, // 队列名称
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
            try {
                log.info("【静态页微服务】商品下架，往本地删除静态页成功，商品ID = {}",spuId);
                pageService.deleteItemHtml(spuId);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("【静态页微服务】商品下架，往本地删除静态页s失败，商品ID = {} , 异常信息是：{}",spuId, e.getMessage());
            }
        }
    }
}
