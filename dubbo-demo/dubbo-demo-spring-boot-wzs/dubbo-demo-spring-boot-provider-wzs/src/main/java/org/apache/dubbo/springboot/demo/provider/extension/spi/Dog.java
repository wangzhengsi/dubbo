package org.apache.dubbo.springboot.demo.provider.extension.spi;

/**
 * @author wangzhengsi
 * @since 2024/2/27 17:22
 */
public class Dog implements Animal {

    @Override
    public void say() {
        System.out.println("Dog");
    }
}
