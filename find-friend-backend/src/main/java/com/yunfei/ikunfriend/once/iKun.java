package com.yunfei.ikunfriend.once;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class iKun {
    @ExcelProperty("ikun编号")
    private String ikunCode;
    @ExcelProperty("ikun名称")
    private String username;
}