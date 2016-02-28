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

        final Undertow server1 = Undertow.builder().addHttpListener(8081, "localhost").setHandler(exchange -> {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
            exchange.getResponseSender().send("Web Server");

        }).build();

        server1.start();

        HttpHandler handler = Handlers.predicates(PredicatedHandlersParser.parse(
                "equals[%{LOCAL_SERVER_NAME}, 'diggerapi.localhost'] -> reverse-proxy[{'http://localhost:8080'}]\n"
                        + "equals[%{LOCAL_SERVER_NAME}, 'diggerweb.localhost'] -> reverse-proxy[{'http://localhost:8081'}]",
                ReverseProxyServer.class.getClassLoader()), ResponseCodeHandler.HANDLE_404);
        SSLHeaderHandler sslhandler = new SSLHeaderHandler(new ProxyPeerAddressHandler(handler));

        String bindAddress = System.getProperty("bind.address", "localhost");

        Undertow reverseProxy = Undertow.builder().setServerOption(UndertowOptions.ENABLE_HTTP2, true)
                .setServerOption(UndertowOptions.ENABLE_SPDY, true).addHttpListener(80, bindAddress)
                .setHandler(sslhandler).build();
        reverseProxy.start();

    }

}