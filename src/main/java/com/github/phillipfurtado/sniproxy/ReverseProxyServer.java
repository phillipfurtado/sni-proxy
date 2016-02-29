package com.github.phillipfurtado.sniproxy;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.ProxyPeerAddressHandler;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.SSLHeaderHandler;
import io.undertow.server.handlers.builder.PredicatedHandlersParser;
import io.undertow.util.Headers;

public class ReverseProxyServer {

    private ReverseProxyServer() {

    }

    public static void main(final String[] args) throws Exception {

        String bindAddress = System.getProperty("bind.address", "localhost");

        final Undertow server1 = Undertow.builder().addHttpListener(8081, bindAddress).setHandler(exchange -> {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            exchange.getResponseSender().send("Digger Web Server");

        }).build();

        final Undertow server2 = Undertow.builder().addHttpListener(8082, bindAddress).setHandler(exchange -> {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            exchange.getResponseSender().send("Digger API Server");

        }).build();

        server1.start();

        server2.start();

        HttpHandler handler = Handlers.predicates(PredicatedHandlersParser.parse(
                "equals[%{LOCAL_SERVER_NAME}, 'diggerapi.localhost'] -> reverse-proxy[{'http://localhost:8082'}]\n"
                        + "equals[%{LOCAL_SERVER_NAME}, 'diggerweb.localhost'] -> reverse-proxy[{'http://localhost:8081'}]",
                ReverseProxyServer.class.getClassLoader()), ResponseCodeHandler.HANDLE_404);
        SSLHeaderHandler sslhandler = new SSLHeaderHandler(new ProxyPeerAddressHandler(handler));

        Undertow reverseProxy = Undertow.builder().setServerOption(UndertowOptions.ENABLE_HTTP2, true)
                .setServerOption(UndertowOptions.ENABLE_SPDY, true).addHttpListener(80, bindAddress)
                .setHandler(sslhandler).build();
        reverseProxy.start();

    }

}