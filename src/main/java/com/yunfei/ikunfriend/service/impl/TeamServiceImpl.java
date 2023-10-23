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
import com.yunfei.ikunfriend.model.dto.TeamJoinDTO;
import com.yunfei.ikunfriend.model.dto.TeamQueryDTO;
import com.yunfei.ikunfriend.model.dto.TeamUpdateDTO;
import com.yunfei.ikunfriend.model.vo.TeamUserVO;
import com.yunfei.ikunfriend.model.vo.UserVO;
import com.yunfei.ikunfriend.service.TeamService;
import com.yunfei.ikunfriend.service.UserService;
import com.yunfei.ikunfriend.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author houyunfei
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2023-10-23 13:05:18
 */
@Service
@Slf4j
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

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
        team.setUserId(loginUser.getId());
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

    @Override
    public List<TeamUserVO> listTeams(TeamQueryDTO teamQueryDto, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if (teamQueryDto != null) {
            Long teamId = teamQueryDto.getId();
            if (teamId != null && teamId > 0) {
                queryWrapper.eq("id", teamId);
            }
            String name = teamQueryDto.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }

            String description = teamQueryDto.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }
            Integer maxNum = teamQueryDto.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }
            Long userId = teamQueryDto.getUserId();
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
            Integer status = teamQueryDto.getStatus();
            if (status == null) {
                status = 0;
            }
            if (status > -1) {
                queryWrapper.eq("status", status);
            }
            if (!isAdmin && !status.equals(TeamStatusEnum.PUBLIC)) {
                throw new BussinessException(Code.PARAMS_ERROR, "只能查看公开的队伍");
            }
            String searchText = teamQueryDto.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(wrapper -> wrapper.like("name", searchText)
                        .or().like("description", searchText));
            }
        }
        //不展示已过期的队伍
        queryWrapper.and(wrapper -> wrapper.gt("expireTime", new Date())
                .or().isNull("expireTime"));


        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        //关联查询用户信息
        //查询队伍和已加入队伍成员信息
        log.info("teamList size:{}", teamList.size());
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if (userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            if (user == null) {
                continue;
            }
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team, teamUserVO);
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            teamUserVO.setCreateUser(userVO);
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateDTO teamUpdateDTO, User loginUser) {
        if (teamUpdateDTO == null) {
            throw new BussinessException(Code.PARAMS_ERROR);
        }
        Long id = teamUpdateDTO.getId();
        if (id == null || id <= 0) {
            throw new BussinessException(Code.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if (oldTeam == null) {
            throw new BussinessException(Code.PARAMS_ERROR);
        }
        if (oldTeam.getUserId() != loginUser.getId() && !userService.isAdmin(loginUser)) {
            throw new BussinessException(Code.NO_AUTH, "只能修改自己创建的队伍");
        }
        if (oldTeam.getStatus().equals(TeamStatusEnum.PASSWORD)) {
            if (StringUtils.isBlank(teamUpdateDTO.getPassword())) {
                throw new BussinessException(Code.PARAMS_ERROR, "加密房间必须要设置密码");
            }
        }

        Team team = new Team();
        BeanUtils.copyProperties(teamUpdateDTO, team);
        return this.updateById(team);

    }

    @Override
    public boolean joinTeam(TeamJoinDTO teamJoinDTO, User loginUser) {
        if (teamJoinDTO == null) {
            throw new BussinessException(Code.PARAMS_ERROR);
        }
        Long userId = loginUser.getId();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", userId);
        long count = userTeamService.count(userTeamQueryWrapper);
        if (count > 5) {
            throw new BussinessException(Code.PARAMS_ERROR, "最多创建和加入五个队伍");
        }
        Long teamId = teamJoinDTO.getTeamId();
        if (teamId == null || teamId <= 0) {
            throw new BussinessException(Code.PARAMS_ERROR);
        }
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BussinessException(Code.PARAMS_ERROR, "队伍不存在");
        }
        if (team.getExpireTime() != null && team.getExpireTime().before(new Date())) {
            throw new BussinessException(Code.PARAMS_ERROR, "队伍已过期");
        }
        if (team.getStatus().equals(TeamStatusEnum.PRIVATE)) {
            throw new BussinessException(Code.NULL_ERROR, "禁止加入私有队伍");
        }
        String password = teamJoinDTO.getPassword();
        if (team.getStatus().equals(TeamStatusEnum.PASSWORD)) {
            if (StringUtils.isBlank(password) || !password.equals(team.getPassword())) {
                throw new BussinessException(Code.PARAMS_ERROR, "密码错误");
            }
        }

        //已加入队伍的人数
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        long count1 = userTeamService.count(userTeamQueryWrapper);
        if (count1 >= team.getMaxNum()) {
            throw new BussinessException(Code.PARAMS_ERROR, "队伍已满");
        }

        //不能重复加入已加入的队伍
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", userId);
        userTeamQueryWrapper.eq("teamId", teamId);
        long count2 = userTeamService.count(userTeamQueryWrapper);
        if (count2 > 0) {
            throw new BussinessException(Code.PARAMS_ERROR, "不能重复加入已加入的队伍");
        }

        //插入用户=>队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        return userTeamService.save(userTeam);
    }
}




