package com.eggert.engineer.task.unit;

import com.eggert.engineer.task.GrpcServerService;
import io.grpc.internal.testing.StreamRecorder;
import net.devh.boot.grpc.examples.lib.HelloReply;
import net.devh.boot.grpc.examples.lib.HelloRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class GrpcServerServiceTest {

    private GrpcServerService myService;

    @BeforeEach
    public void setup() {
        myService = new GrpcServerService();
    }

    @Test
    void testSayHello() throws Exception {
        HelloRequest request = HelloRequest.newBuilder()
                .setName("Test")
                .build();
        StreamRecorder<HelloReply> responseObserver = StreamRecorder.create();
        myService.sayHello(request, responseObserver);
        if (!responseObserver.awaitCompletion(5, TimeUnit.SECONDS)) {
            fail("The call did not terminate in time");
        }
        assertNull(responseObserver.getError());
        List<HelloReply> results = responseObserver.getValues();
        assertEquals(1, results.size());
        HelloReply response = results.get(0);
        assertEquals(HelloReply.newBuilder()
                .setMessage("Hello ==> Test")
                .build(), response);
    }
}
