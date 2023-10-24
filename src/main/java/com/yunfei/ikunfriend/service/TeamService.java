package com.yunfei.ikunfriend.service;

import com.yunfei.ikunfriend.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunfei.ikunfriend.model.domain.User;
import com.yunfei.ikunfriend.model.dto.TeamJoinDTO;
import com.yunfei.ikunfriend.model.dto.TeamQueryDTO;
import com.yunfei.ikunfriend.model.dto.TeamQuitDTO;
import com.yunfei.ikunfriend.model.dto.TeamUpdateDTO;
import com.yunfei.ikunfriend.model.vo.TeamUserVO;

import java.util.List;

/**
 * @author houyunfei
 * @description 针对表【team(队伍)】的数据库操作Service
 * @createDate 2023-10-23 13:05:18
 */
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     *
     * @param team
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * 搜索队伍
     *
     * @param teamQueryDto
     * @param isAdmin
     * @return
     */
    List<TeamUserVO> listTeams(TeamQueryDTO teamQueryDto, boolean isAdmin);

    /**
     * 更新队伍
     *
     * @param team
     * @return
     */
    boolean updateTeam(TeamUpdateDTO teamUpdateDTO, User loginUser);

    /**
     * 加入队伍
     *
     * @param teamJoinDTO
     * @param loginUser
     * @return
     */
    boolean joinTeam(TeamJoinDTO teamJoinDTO, User loginUser);

    boolean quitTeam(TeamQuitDTO teamQuitDTO, User loginUser);

    /**
     * 删除（解散）队伍
     * @param id
     * @param loginUser
     * @return
     */
    boolean deleteTeam(long id, User loginUser);
}
