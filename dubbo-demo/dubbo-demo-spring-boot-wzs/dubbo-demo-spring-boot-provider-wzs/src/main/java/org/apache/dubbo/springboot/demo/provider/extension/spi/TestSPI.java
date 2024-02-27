package org.apache.dubbo.springboot.demo.provider.extension.spi;

import org.apache.dubbo.common.extension.ExtensionLoader;

/**
 * @author wangzhengsi
 * @since 2024/2/27 17:24
 */
public class TestSPI {
    public static void main(String[] args) {
        ExtensionLoader<Animal> extensionLoader = ExtensionLoader.getExtensionLoader(Animal.class);
        Animal myDog = extensionLoader.getExtension("myDog");
        myDog.say();
        Animal myCat = extensionLoader.getExtension("myCat");
        myCat.say();
        // @SPI注解value属性指定默认实现，为空的话会报错
        Animal defaultExtension = extensionLoader.getDefaultExtension();
        defaultExtension.say();
    }
}
