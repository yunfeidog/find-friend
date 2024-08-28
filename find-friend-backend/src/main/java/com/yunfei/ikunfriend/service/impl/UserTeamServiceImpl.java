package com.yunfei.ikunfriend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunfei.ikunfriend.model.domain.UserTeam;
import com.yunfei.ikunfriend.service.UserTeamService;
import com.yunfei.ikunfriend.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author houyunfei
* @description 针对表【user_team(用户-队伍表)】的数据库操作Service实现
* @createDate 2023-10-23 13:07:16
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

}




