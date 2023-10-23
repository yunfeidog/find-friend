package com.yunfei.ikunfriend.service;

import com.yunfei.ikunfriend.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yunfei.ikunfriend.model.domain.User;

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

}
