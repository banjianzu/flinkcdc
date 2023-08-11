package com.cdc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CDCLog {

    private static final Logger log = LoggerFactory.getLogger(CDCLog.class) ;

    public static void info(String info){

        log.info("FlinkCDC程序info信息:" + info);
    }

    public static  void warn(String info){

        log.warn("FlinkCDC程序debug信息:" + info);
    }

    public static void error(String info){
        log.error("FlinkCDC程序error信息:" + info);
    }

}
