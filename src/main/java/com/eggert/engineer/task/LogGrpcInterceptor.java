package com.eggert.engineer.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

/*
Reference: https://github.com/grpc-ecosystem/grpc-spring/blob/master/examples/local-grpc-server/src/main/java/net/devh/boot/grpc/examples/local/server/LogGrpcInterceptor.java
 */
public class LogGrpcInterceptor implements ServerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(LogGrpcInterceptor.class);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> serverCall, Metadata metadata,
            ServerCallHandler<ReqT, RespT> serverCallHandler) {
        log.info(serverCall.getMethodDescriptor().getFullMethodName());
        return serverCallHandler.startCall(serverCall, metadata);
    }
}
