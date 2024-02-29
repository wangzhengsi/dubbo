package org.apache.dubbo.springboot.demo.provider.extension.activate;

import org.apache.dubbo.common.extension.SPI;

/**
 * @author wangzhengsi
 * @since 2024/2/28 17:55
 */
@SPI
public interface TemplateFilter {
    void say();
}
