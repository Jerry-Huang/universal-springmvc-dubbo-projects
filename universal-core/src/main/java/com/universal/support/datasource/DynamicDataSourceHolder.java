package com.universal.support.datasource;

public class DynamicDataSourceHolder {

    private static final ThreadLocal<String> dataSourceHolder = new ThreadLocal<String>();

    public static void set(String name) {
        dataSourceHolder.set(name);
    }

    public static String get() {
        return dataSourceHolder.get();
    }
}
