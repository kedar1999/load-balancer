package com.network.loadbalancer.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service
public class LoadBalancerApplicationContext implements ApplicationContextAware {

    private static ApplicationContext context;

    public LoadBalancerApplicationContext() {
    }

    public void setApplicationContext(ApplicationContext context) throws BeansException {
        LoadBalancerApplicationContext.context = context;
    }

    public static <T> T getBean(String beanName, Class<T> clazz) {
        try {
            return context != null ? context.getBean(beanName, clazz) : null;
        } catch (NoSuchBeanDefinitionException var3) {
            return null;
        }
    }

    public static <T> T getBean(Class<T> clazz) {
        try {
            return context != null ? context.getBean(clazz) : null;
        } catch (NoSuchBeanDefinitionException var2) {
            return null;
        }
    }
}
