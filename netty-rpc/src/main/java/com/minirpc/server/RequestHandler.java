package com.minirpc.server;

import com.minirpc.transport.command.Command;

/**
 * Provider 端的请求处理器
 */
public interface RequestHandler {

    /**
     * 处理请求
     * @param requestCommand 请求命令
     * @return 响应命令
     */
    Command handle(Command requestCommand);

    /**
     * 支持的请求类型
     */
    int type();

}
