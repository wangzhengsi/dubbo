package org.apache.dubbo.springboot.demo.provider.service;

import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.springboot.demo.TempuraService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wangzhengsi
 * @since 2023/12/23 15:40
 */
@DubboService
public class TempuraServiceImpl implements TempuraService {

    private static final Logger logger = LoggerFactory.getLogger(TempuraServiceImpl.class);

    @Override
    public String send(String msg) {
        logger.info("接收到来自{}的服务调用", RpcContext.getServerContext().getRemoteAddress());
        return "back:" + msg;
    }
}
