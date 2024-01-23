package org.jembi.jempi.libapi.httpServer;

public abstract class HttpServerRouteEntries<R, S> implements IHttpServerRouteEntries<R, S> {

   protected S httpServer = null;

   public HttpServerRouteEntries(final S ihttpServer) {
      this.httpServer = ihttpServer;
   }

   @Override
   public abstract R getRouteEntries();
}
