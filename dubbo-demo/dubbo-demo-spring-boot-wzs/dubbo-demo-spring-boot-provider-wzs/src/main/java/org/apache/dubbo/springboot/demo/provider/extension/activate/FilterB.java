package org.apache.dubbo.springboot.demo.provider.extension.activate;

import org.apache.dubbo.common.extension.Activate;

/**
 * @author wangzhengsi
 * @since 2024/2/29 10:17
 */
@Activate(group = {"a3"}) // 传递a3会激活
public class FilterB implements TemplateFilter {
    @Override
    public void say() {
        System.out.println("FilterB#say");
    }
}
