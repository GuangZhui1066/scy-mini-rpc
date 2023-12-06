package com.minirpc.client.stub;

import com.minirpc.transport.Transport;

/**
 * 桩 (Stub)
 *   桩中包含了一个 Transport 对象，用于与真实的 Provider 通信
 */
public interface ServiceStub {

    void setTransport(Transport transport);

}
