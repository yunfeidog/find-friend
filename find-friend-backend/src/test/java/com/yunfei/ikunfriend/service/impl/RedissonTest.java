package com.yunfei.ikunfriend.service.impl;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class RedissonTest {


    @Resource
    private RedissonClient redissonClient;

    @Test
    void test() {
        List<String> ls=new ArrayList<>();
        ls.add("cxk");
        System.out.println("list:"+ls);

        RList<String> list = redissonClient.getList("test-list");
        list.add("cxk");
        System.out.println("redis:"+list);
        list.get(0);
    }
}
