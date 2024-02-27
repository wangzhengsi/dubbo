package org.apache.dubbo.springboot.demo.provider.extension.spi;

import org.apache.dubbo.common.extension.SPI;

/**
 * @author wangzhengsi
 * @since 2024/2/27 17:22
 */
@SPI(value = "myCat")
public interface Animal {
    void say();
}
