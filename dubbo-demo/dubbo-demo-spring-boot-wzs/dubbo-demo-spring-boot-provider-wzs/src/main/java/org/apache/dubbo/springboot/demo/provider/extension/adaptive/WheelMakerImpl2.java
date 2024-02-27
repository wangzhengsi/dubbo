package org.apache.dubbo.springboot.demo.provider.extension.adaptive;

import org.apache.dubbo.common.URL;

/**
 * @author wangzhengsi
 * @since 2024/2/27 18:23
 */
public class WheelMakerImpl2 implements WheelMaker {
    @Override
    public void makeWheelA(URL url) {
        System.out.println("WheelMakerImpl2#makeWheelA");
    }

    @Override
    public void makeWheelB(URL url) {
        System.out.println("WheelMakerImpl2#makeWheelB");
    }

    @Override
    public void makeWheelC(URL url) {
        System.out.println("WheelMakerImpl2#makeWheelC");
    }
}
