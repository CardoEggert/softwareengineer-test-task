package com.eggert.engineer.task;

import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.context.annotation.Configuration;

// Reference: https://github.com/grpc-ecosystem/grpc-spring/blob/master/examples/local-grpc-server/src/main/java/net/devh/boot/grpc/examples/local/server/GlobalInterceptorConfiguration.java
// Using it for seeing what is being logged when calls are made via GRPC
@Configuration(proxyBeanMethods = false)
public class GlobalInterceptorConfiguration {

    @GrpcGlobalServerInterceptor
    LogGrpcInterceptor logServerInterceptor() {
        return new LogGrpcInterceptor();
    }
}
