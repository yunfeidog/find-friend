package com.yunfei.ikunfriend.model.dto;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 队伍
 * @TableName team
 */
@TableName(value ="team")
@Data
public class TeamUpdateDTO implements Serializable {

    private Long id;



    /**
     * 队伍名称
     */
    private String name;

    /**
     * 队伍描述 
     */
    private String description;


    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 队长id
     */
    private Long userId;

    /**
     * 0-公开，1-私有，2-加密 
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;

}