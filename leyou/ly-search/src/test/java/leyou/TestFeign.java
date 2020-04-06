package leyou;

import com.leyou.common.vo.PageResult;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.dto.SpuDTO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
public class TestFeign {

    @Autowired
    private ItemClient itemClient;

    @Test
    public void test(){
        BrandDTO brandDTO = itemClient.queryBrandById(18374L);
        System.out.println("结果为：" + brandDTO.getName());
    }
}