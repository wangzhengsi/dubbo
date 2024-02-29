package org.apache.dubbo.springboot.demo.provider.extension.activate;

import org.apache.dubbo.common.extension.Activate;

/**
 * @author wangzhengsi
 * @since 2024/2/29 10:17
 */
@Activate(group = {"a1,a2"}) // 传递a1或a2都会激活
public class FilterA implements TemplateFilter {
    @Override
    public void say() {
        System.out.println("FilterA#say");
    }
}
