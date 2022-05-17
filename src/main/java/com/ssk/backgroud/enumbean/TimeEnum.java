package com.ssk.backgroud.enumbean;

import java.util.concurrent.TimeUnit;

public enum TimeEnum {
    SECONDS("秒",TimeUnit.SECONDS),
    MINUTE("分钟",TimeUnit.MINUTES),
    HOURS("小时",TimeUnit.HOURS);
    private String name;
    private TimeUnit unit;

    TimeEnum(String name) {
        this.name = name;
    }

    TimeEnum(String name, TimeUnit unit) {
        this.name = name;
        this.unit = unit;
    }

    @Override
    public String toString() {
        return name;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public TimeEnum getTime(String name) {
        TimeEnum[] timeEnums = TimeEnum.values();
        for (TimeEnum timeEnum : timeEnums) {
            if (timeEnum.toString().equals(name)) return timeEnum;
        }
        return MINUTE;
    }
}
