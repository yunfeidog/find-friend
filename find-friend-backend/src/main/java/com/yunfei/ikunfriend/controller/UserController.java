package com.yunfei.ikunfriend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunfei.ikunfriend.common.Code;
import com.yunfei.ikunfriend.common.Result;
import com.yunfei.ikunfriend.common.ResultUtils;
import com.yunfei.ikunfriend.constant.UserConstant;
import com.yunfei.ikunfriend.exception.BussinessException;
import com.yunfei.ikunfriend.model.domain.User;
import com.yunfei.ikunfriend.model.dto.UserLoginDTO;
import com.yunfei.ikunfriend.model.dto.UserRegisterDTO;
import com.yunfei.ikunfriend.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:8000")
@Slf4j
@Api(tags = "用户相关接口")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Result<Long> userRegister(@RequestBody UserRegisterDTO userRegisterDto) {
        if (userRegisterDto == null) {
            throw new BussinessException(Code.PARAMS_ERROR);
        }
        long id = userService.userRegister(userRegisterDto);
        return ResultUtils.success(id);
    }

    @PostMapping("/login")
    @ApiOperation("用户登录")
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
    @ApiOperation("用户注销登录")
    public Result<Integer> logout(HttpServletRequest request) {
        if (request == null) {
            throw new BussinessException(Code.NOT_LOGIN);
        }
        userService.logout(request);
        return ResultUtils.success(1);
    }

    /**
     * 按照用户名搜索用户
     *
     * @param username
     * @param request
     * @return
     */
    @GetMapping("/search")
    @ApiOperation("搜索用户")
    public Result<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BussinessException(Code.NO_AUTH);
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            userQueryWrapper.like("username", username);
        }
        List<User> userList = userService.list(userQueryWrapper);
        List<User> users = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(users);
    }

    @PostMapping("/delete/{id}")
    @ApiOperation("删除用户")
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
    @ApiOperation("获取当前登录用户")
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
    @ApiOperation("根据标签搜索用户")
    public Result<List<User>> searchUsersByTags(@RequestParam List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BussinessException(Code.PARAMS_ERROR);
        }
        List<User> users = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(users);
    }

    @PostMapping("/update")
    @ApiOperation("更新用户信息")
    public Result<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        if (user == null) {
            throw new BussinessException(Code.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Integer flag = userService.updateUser(user, loginUser);
        return ResultUtils.success(flag);
    }

    @GetMapping("/recommend")
    @ApiOperation("用户推荐")
    public Result<Page<User>> recommendUser(int pageSize, int pageNum, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Page<User> userList = userService.recommendUser(pageSize, pageNum,loginUser);
        return ResultUtils.success(userList);
    }

    /**
     * 获取最匹配的用户
     *
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/match")
    @ApiOperation("获取最匹配的用户")
    public Result<List<User>> matchUsers(long num, HttpServletRequest request) {
        if (num <= 0 || num > 20) {
            throw new BussinessException(Code.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        List<User> users = userService.matchUsers(num, loginUser);
        return ResultUtils.success(users);
    }
}
