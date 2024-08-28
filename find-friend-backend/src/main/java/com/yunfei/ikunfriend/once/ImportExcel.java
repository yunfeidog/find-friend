package com.yunfei.ikunfriend.once;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ListUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.Map;


/**
 * 导入Excel数据
 */
@Slf4j
public class ImportExcel {

    public static final String fileName = "/Users/houyunfei/资料/备战秋招/ikun伙伴匹配系统/ikunfriend-back/src/main/resources/testExcel.xlsx";

    public static void main(String[] args) {
        synchronousRead ();
    }

    public static void synchronousRead() {
        // 这里 需要指定读用哪个class去读，然后读取第一个sheet 同步读取会自动finish
        List<iKun> totalList = EasyExcel.read(fileName).head(iKun.class).sheet().doReadSync();
        for (iKun iKun : totalList) {
            System.out.println(iKun);
        }
    }

    private static void method1() {
        // 写法1：JDK8+ ,不用额外写一个DemoDataListener
        // since: 3.0.0-beta1
        // 这里默认每次会读取100条数据 然后返回过来 直接调用使用数据就行
        // 具体需要返回多少行可以在`PageReadListener`的构造函数设置
        EasyExcel.read(fileName, iKun.class, new TableListener()).sheet().doRead();
    }


}
