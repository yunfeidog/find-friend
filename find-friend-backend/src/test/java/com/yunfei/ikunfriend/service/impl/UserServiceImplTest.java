package com.yunfei.ikunfriend.service.impl;

import com.yunfei.ikunfriend.model.domain.User;
import com.yunfei.ikunfriend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
@Slf4j
class UserServiceImplTest {

    @Resource
    private UserService userService;

    @Test
    void searchUsersByTags() {
        List<String> tagNameList = Arrays.asList("java", "python");
        List<User> users = userService.searchUsersByTags(tagNameList);
    }


    @Test
    void test() {

        //List<String> ls=new ArrayList<>();
        //ls.add("cxk");
        //System.out.println("list:"+ls);
        //
        //RList<String> list = redissonClient.getList("test-list");
        //list.add("cxk");
        //System.out.println("redis:"+list);
        //list.get(0);
    }

    @Test
    void matchUsers() {
        int num = 10;
        User user = userService.getById(1001);
            long costTime1 = 0;
            long startTime1 = System.currentTimeMillis();
            for (int i = 0; i < 100; i++) {
                List<User> users1 = userService.matchUsersByListSorted(num, user);
            }
            long endTime1 = System.currentTimeMillis();
            costTime1 = endTime1 - startTime1;


            long startTime2 = System.currentTimeMillis();
            for (int i = 0; i < 100; i++) {
                List<User> users2 = userService.matchUsersByPriorityQueue(num, user);
            }
            long endTime2 = System.currentTimeMillis();
            long costTime2 = endTime2 - startTime2;
            log.info("100运算情况下：List排序耗时:{}ms,优先队列耗时:{}ms,相差:{}ms,优化比例:{}%", costTime1, costTime2, costTime1 - costTime2,
                    (costTime1 - costTime2) * 100 / costTime1);
    }
}