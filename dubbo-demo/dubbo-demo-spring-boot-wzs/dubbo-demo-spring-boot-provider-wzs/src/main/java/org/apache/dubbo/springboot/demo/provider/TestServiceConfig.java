package org.apache.dubbo.springboot.demo.provider;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;
import org.apache.dubbo.springboot.demo.TempuraService;
import org.apache.dubbo.springboot.demo.provider.service.TempuraServiceImpl;

/**
 * @author wangzhengsi
 * @since 2023/12/23 17:26
 */
public class TestServiceConfig {
    public static void main(String[] args) {
        startWithBootstrap();
    }

    private static void startWithBootstrap() {
        ServiceConfig<TempuraServiceImpl> service = new ServiceConfig<>();
        service.setInterface(TempuraService.class);
        service.setRef(new TempuraServiceImpl());
        // 获取DubboBootstrap实例
        DubboBootstrap bootstrap = DubboBootstrap.getInstance();
        bootstrap.application(new ApplicationConfig("my-provider")) // 添加应用程序配置
            .registry(new RegistryConfig("nacos://127.0.0.1:8848")) // 添加注册中心配置
            .protocol(new ProtocolConfig(CommonConstants.DUBBO, 20881)) // 添加协议配置
            .service(service) // 初始化服务配置
            .start() // 启动
            .await();
    }
}
