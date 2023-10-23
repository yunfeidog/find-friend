package com.yunfei.ikunfriend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunfei.ikunfriend.common.Code;
import com.yunfei.ikunfriend.common.TeamStatusEnum;
import com.yunfei.ikunfriend.exception.BussinessException;
import com.yunfei.ikunfriend.mapper.TeamMapper;
import com.yunfei.ikunfriend.model.domain.Team;
import com.yunfei.ikunfriend.model.domain.User;
import com.yunfei.ikunfriend.model.domain.UserTeam;
import com.yunfei.ikunfriend.service.TeamService;
import com.yunfei.ikunfriend.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Optional;

/**
 * @author houyunfei
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2023-10-23 13:05:18
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        //1. 请求参数是否为空
        if (team == null) {
            throw new BussinessException(Code.PARAMS_ERROR);
        }
        //2. 是否登录，未登录不允许创建
        if (loginUser == null) {
            throw new BussinessException(Code.NOT_LOGIN);
        }
        //3. 校验信息
        //    1. 队伍>1 且<=20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BussinessException(Code.PARAMS_ERROR, "队伍人数不满足要求");
        }
        //    2. 队伍标题
        String name = team.getName();
        if (StringUtils.isBlank(name) || name.length() > 20) {
            throw new BussinessException(Code.PARAMS_ERROR, "队伍标题不满足要求");
        }
        //    3. 描述<=512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BussinessException(Code.PARAMS_ERROR, "队伍描述过长");
        }
        //    4. status是否公开
        Integer status = Optional.ofNullable(team.getStatus()).orElse(0);
        if (status < 0 || status > 3) {
            throw new BussinessException(Code.PARAMS_ERROR, "队伍状态不满足要求");
        }
        //    5. 如果是加密，必须要有密码
        String password = team.getPassword();
        if (status.equals(TeamStatusEnum.PASSWORD)) {
            if (StringUtils.isBlank(password) || password.length() > 32) {
                throw new BussinessException(Code.PARAMS_ERROR, "密码设置不正确");
            }
        }
        //    6. 超时时间>当前时间
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new BussinessException(Code.PARAMS_ERROR, "超时时间不正确");
        }
        //    7. 校验用户最多创建五个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        long hasTeamCount = this.count(queryWrapper);
        if (hasTeamCount >= 5) {
            throw new BussinessException(Code.PARAMS_ERROR, "最多创建五个队伍");
        }
        //4. 插入队伍信息到队伍表
        team.setId(null);
        boolean save = this.save(team);
        if (!save) {
            throw new BussinessException(Code.PARAMS_ERROR, "创建队伍失败");
        }
        //5. 插入用户=>队伍关系到关系表
        Long teamId = team.getId();
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(loginUser.getId());
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        boolean save1 = userTeamService.save(userTeam);
        if (!save1) {
            throw new BussinessException(Code.PARAMS_ERROR, "创建队伍失败");
        }

        return teamId;
    }
}




