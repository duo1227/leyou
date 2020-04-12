package com.leyou.page.test;

import com.leyou.page.service.PageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class createItemHtmlTest {

    @Autowired
    private PageService pageService;

    @Test
    public void createItemHtml() {

        List<Long> spuIdList = Arrays.asList(141L, 151L, 161L, 171L, 159L, 181L);
        for (Long id : spuIdList) {
            pageService.createItemHtml(id);
        }

    }
}