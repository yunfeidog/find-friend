package com.yunfei.ikunfriend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yunfei.ikunfriend.common.Code;
import com.yunfei.ikunfriend.constant.RedisConstant;
import com.yunfei.ikunfriend.constant.UserConstant;
import com.yunfei.ikunfriend.exception.BussinessException;
import com.yunfei.ikunfriend.mapper.UserMapper;
import com.yunfei.ikunfriend.model.domain.User;
import com.yunfei.ikunfriend.model.dto.UserLoginDTO;
import com.yunfei.ikunfriend.model.dto.UserRegisterDTO;
import com.yunfei.ikunfriend.service.UserService;
import com.yunfei.ikunfriend.utils.AlgorithmUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
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

    @Resource
    private RedisTemplate redisTemplate;


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
        //在内存中计算结果
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
    public long userRegister(UserRegisterDTO userRegisterDto) {
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
        //查询现在最大的id是多少
        String url = UserConstant.AVATAR_URL.get(new Random().nextInt(UserConstant.AVATAR_URL.size()));
        user.setAvatarUrl(url);
        user.setGender(new Random().nextInt(2) + 1);
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setIkunCode(ikunCode);
        int result = userMapper.insert(user);
        if (result < 1) {
            throw new BussinessException(Code.SYSTEM_ERROR, "注册失败");
        }
        Long userId = user.getId();
        String username = "无敌ikun" + userId + "号";
        user.setUsername(username);
        userMapper.updateById(user);
        return userId;
    }

    @Override
    public User userLogin(UserLoginDTO userLoginDto, HttpServletRequest request) {
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
        User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (user == null) {
            throw new BussinessException(Code.NOT_LOGIN);
        }
        return user;
    }

    @Override
    public List<User> matchUsers(long num, User loginUser) {
//        List<User> users1 = matchUsersByListSorted(num, loginUser);
        List<User> users = matchUsersByPriorityQueue(num, loginUser);
        return users;
    }

    private List<User> matchUsersByListSorted(long num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        List<Pair<User, Long>> list = new ArrayList<>();
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            //无标签或者是自己，跳过
            if (StringUtils.isBlank(userTags) || user.getId().equals(loginUser.getId())) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            int distance = AlgorithmUtils.minDistance(tagList, userTagList);
            list.add(new Pair<>(user, (long) distance));
        }
        //按照编辑距离从小到达排序 编辑距离越小说明相似度越高
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        List<Long> userIdList = topUserPairList.stream().map(userLongPair -> userLongPair.getKey().getId()).collect(Collectors.toList());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper)
                .stream()
                .map(this::getSafetyUser)
                .collect(Collectors.groupingBy(User::getId));
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }

    public List<User> matchUsersByPriorityQueue(long num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        queryWrapper.isNotNull("tags");
        // 获取所有用户列表
        List<User> userList = this.list(queryWrapper);
        // 获取登录用户的标签
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        // 创建一个优先队列，按照距离进行排序
        PriorityQueue<Pair<User, Long>> priorityQueue = new PriorityQueue<>(Comparator.comparing(Pair::getValue));
        // 遍历所有用户，计算距离并加入优先队列
        for (User user : userList) {
            String userTags = user.getTags();
            if (StringUtils.isBlank(userTags) || user.getId().equals(loginUser.getId())) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            int distance = AlgorithmUtils.minDistance(tagList, userTagList);
            priorityQueue.offer(new Pair<>(user, (long) distance));

            // 保持优先队列的大小不超过 num
            if (priorityQueue.size() > num) {
                priorityQueue.poll();
            }
        }
        // 创建一个列表以存储最终的用户
        List<User> finalUserList = new ArrayList<>(priorityQueue.size());
        // 从优先队列中提取前 num个用户
        while (!priorityQueue.isEmpty()) {
            finalUserList.add(priorityQueue.poll().getKey());
        }
        // 反转列表以得到前 num 个匹配用户
        Collections.reverse(finalUserList);
        return finalUserList;
    }


    @Override
    public Page<User> recommendUser(int pageSize, int pageNum, User loginUser) {
        String redisKey = String.format("%s%s", RedisConstant.USER_RECOMMEND, loginUser.getId());
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        if (userPage != null) {
            log.info("get recommend user from redis");
            return userPage;
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Page<User> userList = this.page(new Page<>(pageNum, pageSize), queryWrapper);
        try {
            valueOperations.set(redisKey, userList, 30, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("redis set error:{}", e.getMessage());
        }
        return userList;
    }
}




