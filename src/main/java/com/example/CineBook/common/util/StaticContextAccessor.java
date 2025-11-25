package com.example.CineBook.common.util;


import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class StaticContextAccessor implements ApplicationContextAware {
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // Lấy ApplicationContext và gán vào biến static
        context = applicationContext;
    }

    /**
     * Lấy một Spring bean theo class.
     *
     * @param beanClass Class của bean cần lấy.
     * @param <T>       Kiểu của bean.
     * @return một instance của bean.
     */
    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }
}
