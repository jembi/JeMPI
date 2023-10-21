package org.jempi.jempi.api.httpServer;

import akka.actor.typed.*;
import akka.actor.typed.DispatcherSelector;
import akka.actor.typed.javadsl.Behaviors;
import akka.dispatch.MessageDispatcher;
import akka.http.javadsl.HandlerProvider;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.ServerBuilder;
import com.google.protobuf.MapEntry;
import org.jembi.jempi.api.httpServer.HttpServer;
import org.jembi.jempi.shared.TestUtilities;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HttpServerTest {

    private Http mockHttpServer = null;
    private ActorSystem<Void> mockSystem = null;
    private MessageDispatcher mockDispatcher = null;

    public void mockAkkAHttp(){
        mockHttpServer = Mockito.mock(Http.class);
        ServerBuilder mockServerBuilder = Mockito.mock(ServerBuilder.class);
        Mockito.when(mockHttpServer.newServerAt(Mockito.anyString(), Mockito.anyInt())).thenReturn(mockServerBuilder);
    }
     @BeforeAll
     public void createMockObjects(){
         TestUtilities.SetTestsRunEnvironment(new HashMap<>());
         this.mockAkkAHttp();
         mockSystem = ActorSystem.create(Behaviors.empty(), "TestAPI_KC");
         mockDispatcher = (MessageDispatcher) mockSystem.dispatchers().lookup(DispatcherSelector.fromConfig("akka.actor.default-dispatcher"));
     }

    @Test
    public void testCanOpenServer(){
        HttpServer httpServer = new HttpServer(mockDispatcher);
        httpServer.open("testHost", 50000, mockSystem, null, null);
    }

    @Test
    public void testGetCorrectCors(){

    }

}
