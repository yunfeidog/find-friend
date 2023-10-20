package com.yunfei.ikunfriend.service;

import com.yunfei.ikunfriend.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunfei.ikunfriend.model.dto.UserLoginDto;
import com.yunfei.ikunfriend.model.dto.UserRegisterDto;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author houyunfei
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2023-10-16 08:52:27
 */
public interface UserService extends IService<User> {

    List<User> searchUsersByTags(List<String> tagNameList);

    /**
     * 用户注册
     * @param userRegisterDto 用户注册信息
     * @return 用户id
     */
    long userRegister(UserRegisterDto userRegisterDto);

    /**
     * 用户登录
     *
     * @param userLoginDto 用户登录信息
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(UserLoginDto userLoginDto, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param originalUser
     * @return
     */
    User getSafetyUser(User originalUser);

    /**
     * 用户注销
     *
     * @param request 请求
     * @return
     */
    Integer logout(HttpServletRequest request);

}
