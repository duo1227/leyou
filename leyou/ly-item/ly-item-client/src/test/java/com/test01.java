package com;

import java.util.HashMap;
import java.util.Map;

public class test01 {
    public static void main(String[] args) {
        Long aa = 2L;
        Map<Long,String> map = new HashMap<>();
        map.put(1L,"111");
        map.put(2L,"222");
        map.put(5L,"555");
        map.put(6L,"666");
        System.out.println(map.get(aa));
    }
}
