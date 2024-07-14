package com.eggert.engineer.task;

import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingInterceptor implements ServerInterceptor {

  private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

    log.info("Received gRPC call: {}", call.getMethodDescriptor().getFullMethodName());

    return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(
        next.startCall(call, headers)) {
      @Override
      public void onMessage(ReqT message) {
        log.info("Request parameters: {}", message);
        super.onMessage(message);
      }

      @Override
      public void onComplete() {
        log.info("Finished gRPC call: {}", call.getMethodDescriptor().getFullMethodName());
        super.onComplete();
      }
    };
  }
}
