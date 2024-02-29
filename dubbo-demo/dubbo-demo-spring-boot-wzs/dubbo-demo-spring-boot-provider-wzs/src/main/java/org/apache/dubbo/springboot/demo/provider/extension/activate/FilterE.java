package org.apache.dubbo.springboot.demo.provider.extension.activate;

import org.apache.dubbo.common.extension.Activate;

/**
 * @author wangzhengsi
 * @since 2024/2/29 10:17
 */
@Activate(group = {"a5", "a6"}, value = {"MMM", "HHH"}) // 传递a5或a6 并且url中包含MMM或HHH才会激活
public class FilterE implements TemplateFilter {
    @Override
    public void say() {
        System.out.println("FilterE#say");
    }
}
