package org.apache.dubbo.springboot.demo.provider.extension.adaptive;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Adaptive;
import org.apache.dubbo.common.extension.SPI;

/**
 * @author wangzhengsi
 * @since 2024/2/27 18:21
 */
@SPI("wheelMaker1")
public interface WheelMaker {

    @Adaptive
    void makeWheelA(URL url);

    @Adaptive("key4")
    void makeWheelB(URL url);

    @Adaptive({"key3", "key2", "key1"})
    void makeWheelC(URL url);
}
