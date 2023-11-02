package com.yunfei.ikunfriend.once;

import com.yunfei.ikunfriend.mapper.UserMapper;
import com.yunfei.ikunfriend.model.domain.User;
import com.yunfei.ikunfriend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest
class InsertUsersTest {
    @Resource
    private UserMapper userMapper;

    @Resource
    private UserService userService;

    private ExecutorService executorService = new ThreadPoolExecutor(60, 1000, 10000,
            TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));


    /**
     * 批量插入用户
     */
    @Test
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int NUM = 10000000;
        int batchSize = 50;
        //分10组
        int j = 0;
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            List<User> userList = new ArrayList<>();
            while (true) {
                j++;
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
                userList.add(user);
                if (j % batchSize == 0) break;
            }
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("Thread name:" + Thread.currentThread().getName());
                userService.saveBatch(userList, batchSize);
            }, executorService);
            futures.add(future);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println("总时间：" + stopWatch.getTotalTimeMillis());
    }
}