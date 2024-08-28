package com.yunfei.ikunfriend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunfei.ikunfriend.model.domain.Tag;
import com.yunfei.ikunfriend.service.TagService;
import com.yunfei.ikunfriend.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author houyunfei
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2023-10-20 11:28:55
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService {

}




