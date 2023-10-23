package com.yunfei.ikunfriend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yunfei.ikunfriend.common.Code;
import com.yunfei.ikunfriend.common.UserConstant;
import com.yunfei.ikunfriend.exception.BussinessException;
import com.yunfei.ikunfriend.model.domain.User;
import com.yunfei.ikunfriend.model.dto.UserLoginDto;
import com.yunfei.ikunfriend.model.dto.UserRegisterDto;
import com.yunfei.ikunfriend.service.UserService;
import com.yunfei.ikunfriend.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author houyunfei
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2023-10-16 08:52:27
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    private static final String SALT = "yunfei";

    @Resource
    private UserMapper userMapper;


    /**
     * 根据标签搜索用户
     *
     * @param tagNameList 标签列表
     * @return 用户列表
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BussinessException(Code.PARAMS_ERROR);
        }
        //return searchUsersByTagsBySQL(tagNameList);
        return searchUsersByTagsByMemory(tagNameList);
    }

    private List<User> searchUsersByTagsByMemory(List<String> tagNameList) {
        //先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        //在内存中判断：
        Gson gson = new Gson();
        List<User> users = userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            if (StringUtils.isBlank(tagsStr)) {
                return false;
            }
            Set<String> set = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            set = Optional.ofNullable(set).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if (!set.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
        return users;
    }

    @Deprecated //废弃
    private List<User> searchUsersByTagsBySQL(List<String> tagNameList) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        List<User> users = userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
        return users;
    }


    @Override
    public long userRegister(UserRegisterDto userRegisterDto) {
        String userAccount = userRegisterDto.getUserAccount();
        String userPassword = userRegisterDto.getUserPassword();
        String checkPassword = userRegisterDto.getCheckPassword();
        String ikunCode = userRegisterDto.getIkunCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, ikunCode)) {
            throw new BussinessException(Code.PARAMS_ERROR, "参数不能为空");
        }
        if (userAccount.length() < 4) {
            throw new BussinessException(Code.PARAMS_ERROR, "账户长度不能小于4");
        }
        if (userPassword.length() < 4 || checkPassword.length() < 4) {
            throw new BussinessException(Code.PARAMS_ERROR, "密码长度不能小于4");
        }
        if (ikunCode.length() > 10) {
            throw new BussinessException(Code.PARAMS_ERROR, "ikun编号长度不能大于10");
        }

        //账户不能包含字符
        //账户不能包含特殊字符
        String validPattern = "^[a-zA-Z0-9_]+$";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (!matcher.find()) {
            throw new BussinessException(Code.PARAMS_ERROR, "账户不能包含特殊字符");
        }
        //密码和确认密码必须一致
        if (!userPassword.equals(checkPassword)) {
            throw new BussinessException(Code.PARAMS_ERROR, "密码和确认密码必须一致");
        }
        //账户不能重复
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(userQueryWrapper);
        if (count > 0) {
            throw new BussinessException(Code.PARAMS_ERROR, "账户已存在");
        }

        //ikunCode不能重复
        userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("ikunCode", ikunCode);
        count = userMapper.selectCount(userQueryWrapper);
        if (count > 0) {
            throw new BussinessException(Code.PARAMS_ERROR, "ikun编号已存在");
        }
        //加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        //插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setIkunCode(ikunCode);
        int result = userMapper.insert(user);
        if (result < 1) {
            throw new BussinessException(Code.SYSTEM_ERROR, "注册失败");
        }
        return user.getId();
    }

    @Override
    public User userLogin(UserLoginDto userLoginDto, HttpServletRequest request) {
        String userAccount = userLoginDto.getUserAccount();
        String userPassword = userLoginDto.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BussinessException(Code.PARAMS_ERROR, "参数不能为空");
        }
        if (userAccount.length() < 4) {
            throw new BussinessException(Code.PARAMS_ERROR, "账户长度不能小于4");
        }
        if (userPassword.length() < 4) {
            throw new BussinessException(Code.PARAMS_ERROR, "密码长度不能小于4");
        }

        //账户不能包含字符
        //账户不能包含特殊字符
        String validPattern = "^[a-zA-Z0-9_]+$";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (!matcher.find()) {
            throw new BussinessException(Code.PARAMS_ERROR, "账户不能包含特殊字符");
        }

        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        //查询用户是否存在
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount", userAccount);
        userQueryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(userQueryWrapper);
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BussinessException(Code.PARAMS_ERROR, "账户或密码错误");
        }
        //用户脱敏
        User safetyUser = getSafetyUser(user);
        //记录用户登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return safetyUser;
    }

    @Override
    public User getSafetyUser(User user) {
        if (user == null) {
            throw new BussinessException(Code.SYSTEM_ERROR, "用户不存在");
        }
        User safetyUser = new User();
        safetyUser.setId(user.getId());
        safetyUser.setUsername(user.getUsername());
        safetyUser.setUserAccount(user.getUserAccount());
        safetyUser.setAvatarUrl(user.getAvatarUrl());
        safetyUser.setGender(user.getGender());
        safetyUser.setPhone(user.getPhone());
        safetyUser.setEmail(user.getEmail());
        safetyUser.setUserStatus(user.getUserStatus());
        safetyUser.setUserRole(user.getUserRole());
        safetyUser.setCreateTime(user.getCreateTime());
        safetyUser.setIkunCode(user.getIkunCode());
        safetyUser.setTags(user.getTags());
        return safetyUser;
    }

    @Override
    public Integer logout(HttpServletRequest request) {
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return 1;
    }

    @Override
    public Integer updateUser(User user, User loginUser) {
        Long userId = user.getId();
        if (userId <= 0) {
            throw new BussinessException(Code.PARAMS_ERROR);
        }
        //如果是管理员，允许更新任意用户
        //如果是普通用户，只能更新自己的信息
        if (!isAdmin(loginUser) && !userId.equals(loginUser.getId())) {
            throw new BussinessException(Code.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BussinessException(Code.PARAMS_ERROR);
        }
        return userMapper.updateById(user);
    }

    /**
     * 判断是不是管理员
     *
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        //仅管理员可以查询
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (user == null || user.getUserRole() != UserConstant.ADMIN_USER_ROLE) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == UserConstant.ADMIN_USER_ROLE;
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            throw new BussinessException(Code.NOT_LOGIN);
        }
        User user =(User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (user == null) {
            throw new BussinessException(Code.NOT_LOGIN);
        }
        return user;
    }
}




