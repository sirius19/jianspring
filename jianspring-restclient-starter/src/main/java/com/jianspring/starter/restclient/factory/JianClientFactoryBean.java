package com.jianspring.starter.restclient.factory;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

public class JianClientFactoryBean<T> implements FactoryBean<T> {

    private Class<T> clientClass;

    @Autowired
    private JianClientProxyFactory proxyFactory;

    @Override
    public T getObject() throws Exception {
        return proxyFactory.createClient(clientClass);
    }

    @Override
    public Class<?> getObjectType() {
        return clientClass;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setClientClass(Class<T> clientClass) {
        this.clientClass = clientClass;
    }

    public void setClientClass(String clientClassName) throws ClassNotFoundException {
        this.clientClass = (Class<T>) Class.forName(clientClassName);
    }
}