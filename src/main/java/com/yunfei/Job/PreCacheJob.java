package com.yunfei.Job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sun.corba.se.spi.ior.ObjectKey;
import com.yunfei.ikunfriend.model.domain.User;
import com.yunfei.ikunfriend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热任务
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, ObjectKey> redisTemplate;

    private List<Long> mainUserList = Arrays.asList(1L);

    @Resource
    private RedissonClient redissonClient;

    //每天的23点59执行
    @Scheduled(cron = "0 59 23 * *  *")
    public void doCacheRecommendUser() {
        String redisKey1 = "ikun:precacheJob:docache:lock";
        RLock lock = redissonClient.getLock(redisKey1);
        try {
            if (lock.tryLock(0, 30000, TimeUnit.MICROSECONDS)) {
                for (Long userId : mainUserList) {
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> page = userService.page(new Page<>(1, 10), queryWrapper);
                    String redisKey = String.format("ikun:user:recommend:%s", userId);
                    ValueOperations valueOperations = redisTemplate.opsForValue();
                    try {
                        valueOperations.set(redisKey, page.getRecords());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            //只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

}
