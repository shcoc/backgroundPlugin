package com.ssk.backgroud.config;

import java.io.File;

public class BackgroundConfig {
    public static final String FILE_PATH = "file_path";
    public static final String LOCATION = "local";
    public static  final String CIRCULATION_METHOD = "circulation_method";
    public static  final String TIME = "time";
    public static final String AREA = "area";

    public static final String OPACITY = "opacity";


    public static final String BACKGROUD =  new File(System.getProperty("user.home"),".ideaBackground").getAbsolutePath();
}
