package com.yunfei.ikunfriend.service;

import com.yunfei.ikunfriend.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunfei.ikunfriend.model.dto.UserLoginDTO;
import com.yunfei.ikunfriend.model.dto.UserRegisterDTO;

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
     *
     * @param userRegisterDto 用户注册信息
     * @return 用户id
     */
    long userRegister(UserRegisterDTO userRegisterDto);

    /**
     * 用户登录
     *
     * @param userLoginDto 用户登录信息
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(UserLoginDTO userLoginDto, HttpServletRequest request);

    /**
     * 用户脱敏
     *
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


    /**
     * 更新用户信息
     *
     * @param user
     * @return
     */
    Integer updateUser(User user, User loginUser);


    boolean isAdmin(User loginUser);
    boolean isAdmin(HttpServletRequest request);


    /**
     * 获取当前登录用户信息
     *
     * @return
     */
    User getLoginUser(HttpServletRequest request);


    List<User> matchUsers(long num, User loginUser);
}
