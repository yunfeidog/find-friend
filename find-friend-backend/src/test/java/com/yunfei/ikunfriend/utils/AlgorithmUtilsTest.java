package com.yunfei.ikunfriend.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yunfei.ikunfriend.mapper.UserMapper;
import com.yunfei.ikunfriend.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class AlgorithmUtilsTest {

    @Resource
    private UserMapper userMapper;

    @Test
    void minDistance() {
        List<String> stringList = Arrays.asList("Java", "大一", "男");
        List<String> stringList1 = Arrays.asList("Java", "大一", "女");
        List<String> stringList2 = Arrays.asList("Java", "大三", "女");
        int minDistance = AlgorithmUtils.minDistance(stringList, stringList1);
        int minDistance1 = AlgorithmUtils.minDistance(stringList, stringList2);
        System.out.println(minDistance);
        System.out.println(minDistance1);
    }

    @Test
    void testUserTages(){
        //id为1的用户
        User user1 = userMapper.selectById(1);
        //id为1001的用户
        User user2 = userMapper.selectById(1001);
        Gson gson = new Gson();
        List<String> userTagList1 = gson.fromJson(user1.getTags(), new TypeToken<List<String>>() {
        }.getType());
        List<String> userTagList2 = gson.fromJson(user2.getTags(), new TypeToken<List<String>>() {
        }.getType());
        int distance = AlgorithmUtils.minDistance(userTagList1, userTagList2);
        System.out.println(distance);
    }



}