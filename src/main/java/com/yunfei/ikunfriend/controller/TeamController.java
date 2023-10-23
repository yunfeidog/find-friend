package com.yunfei.ikunfriend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yunfei.ikunfriend.common.Code;
import com.yunfei.ikunfriend.common.Result;
import com.yunfei.ikunfriend.common.ResultUtils;
import com.yunfei.ikunfriend.exception.BussinessException;
import com.yunfei.ikunfriend.model.domain.Team;
import com.yunfei.ikunfriend.model.domain.User;
import com.yunfei.ikunfriend.model.dto.TeamAddDto;
import com.yunfei.ikunfriend.model.dto.TeamQueryDto;
import com.yunfei.ikunfriend.service.TeamService;
import com.yunfei.ikunfriend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/team")
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
public class TeamController {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private RedisTemplate redisTemplate;

    @PostMapping("/add")
    public Result<Long> addTeam(@RequestBody TeamAddDto teamAddDto, HttpServletRequest request) {
        if (teamAddDto == null) {
            throw new BussinessException(Code.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddDto, team);
        long teamId = teamService.addTeam(team, loginUser);
        if (teamId <= 0) {
            throw new BussinessException(Code.SYSTEM_ERROR, "添加队伍失败");
        }
        return ResultUtils.success(team.getId());
    }

    @PostMapping("/delete")
    public Result<Boolean> deleteTeam(@RequestBody long id) {
        if (id <= 0) {
            throw new BussinessException(Code.PARAMS_ERROR);
        }
        boolean b = teamService.removeById(id);
        if (!b) {
            throw new BussinessException(Code.SYSTEM_ERROR, "删除队伍失败");
        }
        return ResultUtils.success(b);
    }

    @PostMapping("/update")
    public Result<Boolean> updateTeam(@RequestBody Team team) {
        if (team == null) {
            throw new BussinessException(Code.PARAMS_ERROR);
        }
        boolean b = teamService.updateById(team);
        if (!b) {
            throw new BussinessException(Code.SYSTEM_ERROR, "更新队伍失败");
        }
        return ResultUtils.success(b);
    }

    @GetMapping("/get")
    public Result<Team> getTeamById(long id) {
        if (id <= 0) {
            throw new BussinessException(Code.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BussinessException(Code.SYSTEM_ERROR, "获取队伍失败");
        }
        return ResultUtils.success(team);
    }

    @GetMapping("/list")
    public Result<List<Team>> listTeam(TeamQueryDto teamQueryDto) {
        if (teamQueryDto == null) {
            throw new BussinessException(Code.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQueryDto, team);
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();

        List<Team> teamList = teamService.list(queryWrapper);
        if (teamList == null) {
            throw new BussinessException(Code.SYSTEM_ERROR, "获取队伍列表失败");
        }
        return ResultUtils.success(teamList);
    }

    @GetMapping("/list/page")
    public Result<Page<Team>> listTeamsByPage(TeamQueryDto teamQueryDto) {
        if (teamQueryDto == null) {
            throw new BussinessException(Code.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQueryDto, team);
        Page<Team> page = new Page<>(teamQueryDto.getPageNum(), teamQueryDto.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> teamPage = teamService.page(page, queryWrapper);
        return ResultUtils.success(teamPage);
    }


}
