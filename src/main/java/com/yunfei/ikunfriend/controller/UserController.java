package com.yunfei.ikunfriend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yunfei.ikunfriend.common.Code;
import com.yunfei.ikunfriend.common.Result;
import com.yunfei.ikunfriend.common.ResultUtils;
import com.yunfei.ikunfriend.common.UserConstant;
import com.yunfei.ikunfriend.exception.BussinessException;
import com.yunfei.ikunfriend.model.domain.User;
import com.yunfei.ikunfriend.model.dto.UserLoginDto;
import com.yunfei.ikunfriend.model.dto.UserRegisterDto;
import com.yunfei.ikunfriend.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public Result<Long> userRegister(@RequestBody UserRegisterDto userRegisterDto) {
        if (userRegisterDto == null) {
            throw new BussinessException(Code.PARAMS_ERROR);
        }
        long id = userService.userRegister(userRegisterDto);
        return ResultUtils.success(id);
    }

    @PostMapping("/login")
    public Result<User> userLogin(@RequestBody UserLoginDto userLoginDto, HttpServletRequest request) {
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
}
