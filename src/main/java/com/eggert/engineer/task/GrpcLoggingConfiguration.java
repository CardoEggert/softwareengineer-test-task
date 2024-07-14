package com.eggert.engineer.task;

import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcLoggingConfiguration {

  @GrpcGlobalServerInterceptor
  LoggingInterceptor grpcLoggingInterceptor() {
    return new LoggingInterceptor();
  }
}
