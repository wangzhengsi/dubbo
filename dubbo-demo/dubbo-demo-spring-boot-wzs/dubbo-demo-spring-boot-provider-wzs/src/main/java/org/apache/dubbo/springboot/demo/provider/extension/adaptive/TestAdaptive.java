package org.apache.dubbo.springboot.demo.provider.extension.adaptive;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.ExtensionLoader;

/**
 * @author wangzhengsi
 * @since 2024/2/27 18:26
 */
public class TestAdaptive {
    public static void main(String[] args) {
        URL url = URL.valueOf("dubbo://127.0.0.1:8080/test?key1=wheelMaker1&key3=wheelMaker3&wheel.maker=wheelMaker2");
        WheelMaker wheelMaker = ExtensionLoader.getExtensionLoader(WheelMaker.class).getAdaptiveExtension();
        // 这里makeWheelA方法上面标注了@Adaptive注解，这里value属性为空，则会从url中查找wheel.maker对应的value，这里wheel.maker是接口WheelMark的转换，将驼峰处分开并转换成小写，以"."连接起来，然后就找到实现类wheelMaker2了
        wheelMaker.makeWheelA(url);
        // 这里makeWheelB方法上标注了 @Adaptive(“key4”)，这里指定了key4，但是我们的url中没有传key4，这里会匹配不到，然后他就会走到我们的在SPI注解中指定的默认实现wheelMaker1
        wheelMaker.makeWheelB(url);
        // 这里makeWheelC方法上标注了 @Adaptive({“key3”,“key2”,“key1”})，这里指定了3个，会从url中按顺序查找，这里先查找key3，然后就找到了wheelMaker3，如果没找到key3，再往下找key2
        wheelMaker.makeWheelC(url);
    }
}
