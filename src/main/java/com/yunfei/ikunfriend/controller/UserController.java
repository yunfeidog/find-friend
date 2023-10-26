package com.yunfei.ikunfriend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunfei.ikunfriend.common.Code;
import com.yunfei.ikunfriend.common.Result;
import com.yunfei.ikunfriend.common.ResultUtils;
import com.yunfei.ikunfriend.common.UserConstant;
import com.yunfei.ikunfriend.exception.BussinessException;
import com.yunfei.ikunfriend.model.domain.User;
import com.yunfei.ikunfriend.model.dto.UserLoginDTO;
import com.yunfei.ikunfriend.model.dto.UserRegisterDTO;
import com.yunfei.ikunfriend.model.vo.UserVO;
import com.yunfei.ikunfriend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    @PostMapping("/register")
    public Result<Long> userRegister(@RequestBody UserRegisterDTO userRegisterDto) {
        if (userRegisterDto == null) {
            throw new BussinessException(Code.PARAMS_ERROR);
        }
        long id = userService.userRegister(userRegisterDto);
        return ResultUtils.success(id);
    }

    @PostMapping("/login")
    public Result<User> userLogin(@RequestBody UserLoginDTO userLoginDto, HttpServletRequest request) {
        if (userLoginDto == null) {
            throw new BussinessException(Code.PARAMS_ERROR);
        }
        String userAccount = userLoginDto.getUserAccount();
        String userPassword = userLoginDto.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BussinessException(Code.PARAMS_ERROR);
        }
        User user = userService.userLogin(userLoginDto, request);
        return ResultUtils.success(user);
    }

    @PostMapping("/logout")
    public Result<Integer> logout(HttpServletRequest request) {
        if (request == null) {
            throw new BussinessException(Code.NOT_LOGIN);
        }
        userService.logout(request);
        return ResultUtils.success(1);
    }

    @GetMapping("/search")
    public Result<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BussinessException(Code.NO_AUTH);
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            userQueryWrapper.like("username", username);
        }
        List<User> userList = userService.list(userQueryWrapper);
        List<User> users = userList.stream().map(user -> {
            return userService.getSafetyUser(user);
        }).collect(Collectors.toList());
        return ResultUtils.success(users);
    }

    @PostMapping("/delete/{id}")
    public Result<Boolean> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BussinessException(Code.NO_AUTH);
        }
        if (id <= 0) {
            throw new BussinessException(Code.PARAMS_ERROR);
        }
        boolean flag = userService.removeById(id);
        return ResultUtils.success(flag);
    }


    @GetMapping("/current")
    public Result<User> gerCurrentUser(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (user == null) {
            throw new BussinessException(Code.NOT_LOGIN);
        }
        Long userId = user.getId();
        User user1 = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user1);
        return ResultUtils.success(safetyUser);
    }

    @GetMapping("/search/tags")
    public Result<List<User>> searchUsersByTags(@RequestParam List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BussinessException(Code.PARAMS_ERROR);
        }
        List<User> users = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(users);
    }

    @PostMapping("/update")
    public Result<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        if (user == null) {
            throw new BussinessException(Code.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Integer flag = userService.updateUser(user, loginUser);
        return ResultUtils.success(flag);
    }

    @GetMapping("/recommend")
    public Result<Page<User>> recommendUser(int pageSize, int pageNum, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String redisKey=String.format("ikun:user:recommend:%s",loginUser.getId());
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        if (userPage!=null){
            log.info("get recommend user from redis");
            return ResultUtils.success(userPage);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Page<User> userList = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        try {
            valueOperations.set(redisKey,userList,30, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("redis set error:{}",e.getMessage());
        }
        return ResultUtils.success(userList);
    }

    @GetMapping("/match")
    public Result<List<User>> matchUsers(long num, HttpServletRequest request){
        if (num<=0 ||num>20){
            throw new BussinessException(Code.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUsers(num,loginUser));
    }
}
