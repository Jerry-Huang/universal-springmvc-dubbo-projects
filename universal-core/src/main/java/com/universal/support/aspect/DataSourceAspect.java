package com.universal.support.aspect;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.universal.support.annotation.DataSource;
import com.universal.support.datasource.DynamicDataSourceHolder;

public class DataSourceAspect {

    private final static Logger logger = LoggerFactory.getLogger(DataSourceAspect.class);

    private String defaultDataSource;

    public void before(JoinPoint point) {

        final Object target = point.getTarget();
        final MethodSignature signature = (MethodSignature) point.getSignature();

        final String methodName = signature.getName();
        final Class<?>[] clazzes = target.getClass().getInterfaces();
        final Class<?>[] parameterTypes = signature.getMethod().getParameterTypes();

        try {
            final Method method = clazzes[0].getMethod(methodName, parameterTypes);
            if (method != null) {

                DataSource dataSource = null;

                if (method.isAnnotationPresent(DataSource.class)) {
                    dataSource = method.getAnnotation(DataSource.class);
                    DynamicDataSourceHolder.set(dataSource.value());
                } else if (clazzes[0].isAnnotationPresent(DataSource.class)) {
                    dataSource = clazzes[0].getAnnotation(DataSource.class);
                    DynamicDataSourceHolder.set(dataSource.value());
                } else {
                    DynamicDataSourceHolder.set(defaultDataSource);
                }

                logger.debug("Use dataSource {}.", DynamicDataSourceHolder.get());
            }

        } catch (Exception e) {
            logger.error("Cut-in datasource failed.", e);
        }
    }

    public String getDefaultDataSource() {
        return defaultDataSource;
    }

    public void setDefaultDataSource(String defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
    }

}
