package org.apache.dubbo.springboot.demo.provider.extension.activate;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.ExtensionLoader;

import java.util.List;

/**
 * @author wangzhengsi
 * @since 2024/2/28 18:01
 */
public class ActivateTest {
    public static void main(String[] args) {
        ExtensionLoader<TemplateFilter> loader = ExtensionLoader.getExtensionLoader(TemplateFilter.class);
        URL url = URL.valueOf("test://localhost/test");
        List<TemplateFilter> list;

        list = loader.getActivateExtension(url, "", "a1");
        print(list);

        url = url.addParameter("MMM", "test");
        list = loader.getActivateExtension(url, "", "a5");
        print(list);
    }

    private static void print(List<TemplateFilter> list) {
        for (TemplateFilter filter : list) {
            Class<? extends TemplateFilter> aClass = filter.getClass();
            System.out.println(aClass);
        }
    }
}
