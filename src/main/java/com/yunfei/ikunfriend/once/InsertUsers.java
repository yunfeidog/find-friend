package com.yunfei.ikunfriend.once;
import java.util.Date;

import com.yunfei.ikunfriend.mapper.UserMapper;
import com.yunfei.ikunfriend.model.domain.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

@Component
public class InsertUsers {

    @Resource
    private UserMapper userMapper;


    /**
     * 批量插入用户
     */
    public void  doInsertUsers(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int NUM=10;
        for (int i = 0; i < NUM; i++) {
            User user = new User();
            user.setUsername("假ikun");
            user.setUserAccount("fakeIkun");
            user.setAvatarUrl("https://s2.loli.net/2023/10/16/QRiUYmDLB2vZuE6.webp");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("123");
            user.setEmail("123@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setIkunCode("1212121");
            user.setTags("[]");
            userMapper.insert(user);
        }
        stopWatch.stop();
        System.out.println("总时间："+stopWatch.getTotalTimeMillis());

    }
}
