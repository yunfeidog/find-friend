package com.yunfei.ikunfriend.service.impl;

import com.yunfei.ikunfriend.model.domain.User;
import com.yunfei.ikunfriend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
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
}