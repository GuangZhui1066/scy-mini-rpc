package com.minirpc.client.stub;

import java.util.concurrent.ExecutionException;

import com.minirpc.client.util.RequestIdUtil;
import com.minirpc.serialize.SerializeSupport;
import com.minirpc.transport.Transport;
import com.minirpc.transport.command.Command;
import com.minirpc.transport.command.Header;
import com.minirpc.transport.command.ResponseHeader;
import com.minirpc.transport.command.ResultCode;
import com.minirpc.transport.command.request.RequestType;
import com.minirpc.transport.command.request.RpcRequest;

/**
 * 桩的抽象实现
 */
public class AbstractStub implements ServiceStub {

    protected Transport transport;

    @Override
    public void setTransport(Transport transport) {
        this.transport = transport;
    }


    /**
     * 通过网络发送请求，调用远程服务
     */
    protected byte [] invokeRpcRemote(RpcRequest request) {
        Header header = new Header(RequestType.RPC_REQUEST.getCode(), 1, RequestIdUtil.next());
        byte [] payload = SerializeSupport.serialize(request);
        // 构建完整的网络请求：请求头 + 请求体
        Command requestCommand = new Command(header, payload);
        try {
            // 通过 Transport 发送网络请求，并获取响应
            Command responseCommand = transport.send(requestCommand).get();
            ResponseHeader responseHeader = (ResponseHeader) responseCommand.getHeader();
            if (ResultCode.SUCCESS.getCode() == responseHeader.getCode()) {
                return responseCommand.getPayload();
            } else {
                throw new Exception(responseHeader.getErrMsg());
            }

        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
