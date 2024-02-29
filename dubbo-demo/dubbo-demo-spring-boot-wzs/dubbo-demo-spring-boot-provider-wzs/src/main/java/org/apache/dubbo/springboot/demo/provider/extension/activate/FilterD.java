package org.apache.dubbo.springboot.demo.provider.extension.activate;

import org.apache.dubbo.common.extension.Activate;

/**
 * @author wangzhengsi
 * @since 2024/2/29 10:17
 */
@Activate(group = "a4", order = 4) // 传递a4会激活，排序为4
public class FilterD implements TemplateFilter {
    @Override
    public void say() {
        System.out.println("FilterD#say");
    }
}
