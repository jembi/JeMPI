//package org.jembi.jempi.monitor.operations.data.ops;
//
//import akka.actor.typed.ActorRef;
//import akka.actor.typed.javadsl.AskPattern;
//
//import java.util.concurrent.CompletionStage;
//
//
//public final class Delete {
//
//    public record Response() { }
//
//    public record Request( ActorRef<Delete.Response> replyTo) {}
//
//    static void Handler(){
//
//    }
//    static void AskDelete(ActorRef<Void> processor){
//        CompletionStage<Delete.Response> stage = AskPattern.ask(processor, replyTo -> new Delete.Request(replyTo));
//    }
//}
