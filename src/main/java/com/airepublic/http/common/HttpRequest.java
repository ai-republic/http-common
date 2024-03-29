package com.airepublic.http.common;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.util.Objects;

public class HttpRequest {
    private String host;
    private int port = -1;
    private String method;
    private String scheme;
    private String version;
    private String path;
    private String query;
    private Headers headers;
    private ByteBuffer body;


    public HttpRequest() {
        headers = new Headers();
    }


    public HttpRequest(final URI uri) {
        this(uri, new Headers());
    }


    public HttpRequest(final URI uri, final Headers headers) {
        setUri(uri);
        withHeaders(headers);
    }


    public HttpRequest(final String requestLine, final Headers headers) {
        this(requestLine, headers, null);
    }


    public HttpRequest(final String requestLine, final Headers headers, final ByteBuffer body) {
        setRequestLine(requestLine);
        this.headers = headers;
        this.body = body;
    }


    public Headers getHeaders() {
        return headers;
    }


    public HttpRequest withHeaders(final Headers headers) {
        this.headers = headers;
        return this;
    }


    public String getRequestLine() {
        return (getMethod() != null ? getMethod() : "GET") + " " + (getPath() != null && !getPath().isBlank() ? getPath() : "/") + (getQuery() != null ? "?" + getQuery() : "") + " " + (getVersion() != null ? getVersion() : "HTTP/1.1");
    }


    public void setRequestLine(final String requestLine) {
        if (requestLine != null) {
            final String[] requestParts = requestLine.split(" ");
            final String[] requestQuery = requestParts[1].split("\\?");

            method = requestParts[0];
            path = requestQuery[0];

            if (requestQuery.length > 1) {
                query = requestQuery[1];
            }

            version = requestParts[2];
            scheme = "http";
        }
    }


    public ByteBuffer getBody() {
        return body;
    }


    public void setBody(final ByteBuffer body) {
        this.body = body;
    }


    public HttpRequest withBody(final ByteBuffer body) {
        this.body = body;
        return this;
    }


    public ByteBuffer getHeaderBuffer() throws UnsupportedEncodingException {
        final StringBuffer str = new StringBuffer();
        str.append(getRequestLine() + "\r\n");

        if (headers != null) {
            final StringBuffer headerBuf = headers.entrySet().stream().map(entry -> {
                final StringBuffer buf = new StringBuffer();
                entry.getValue().stream().forEach(value -> buf.append(entry.getKey() + ": " + value + "\r\n"));
                return buf;
            }).collect(StringBuffer::new, StringBuffer::append, StringBuffer::append);

            str.append(headerBuf);
            str.append("\r\n");
        }

        return ByteBuffer.wrap(str.toString().getBytes());
    }


    public URI getUri() throws URISyntaxException {
        return new URI(getScheme() + "://" + getHost() + ":" + getPort() + (getPath() != null ? getPath() : "") + (getQuery() == null || getQuery().isBlank() ? "" : "?" + getQuery()));
    }


    public void setUri(final URI uri) {
        host = uri.getHost();

        if (uri.getPort() != -1) {
            port = uri.getPort();
        } else {
            port = -1;
        }

        scheme = uri.getScheme();
        path = uri.getPath();
        query = uri.getQuery();
    }


    public String getMethod() {
        return method;
    }


    public HttpRequest withMethod(final String method) {
        this.method = method;

        return this;
    }


    public String getHost() {
        if (host == null) {
            if (headers != null) {
                final String hosts = headers.getFirst(Headers.HOST);

                if (hosts != null) {
                    final String[] hostport = hosts.split(":");
                    host = hostport[0];
                }
            }
        }
        return host;
    }


    public HttpRequest withHost(final String host) {
        this.host = host;

        return this;
    }


    public int getPort() {
        if (port == -1) {
            if (headers != null) {
                final String hosts = headers.getFirst(Headers.HOST);

                if (hosts != null) {
                    final String[] hostport = hosts.split(":");

                    if (hostport.length > 1) {
                        port = Integer.parseInt(hostport[1].strip());
                    }
                }
            }
        }
        return port;
    }


    public HttpRequest withPort(final int port) {
        this.port = port;

        return this;
    }


    public String getPath() {
        return path;
    }


    public HttpRequest withPath(final String path) {
        this.path = path;

        return this;
    }


    public String getQuery() {
        return query;
    }


    public HttpRequest withQuery(final String query) {
        this.query = query;

        return this;
    }


    public String getScheme() {
        return scheme;
    }


    public HttpRequest withScheme(final String scheme) {
        this.scheme = scheme;

        return this;
    }


    public String getVersion() {
        return version;
    }


    public HttpRequest withVersion(final String version) {
        this.version = version;
        return this;
    }


    public boolean isSecure() {
        return getScheme().equals("https") || getScheme().equals("wss");
    }


    public Principal getUserPrincipal() {
        // TODO implement user handling
        return null;
    }


    @Override
    public int hashCode() {
        return Objects.hash(body, headers, host, method, path, port, query, scheme, version);
    }


    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HttpRequest other = (HttpRequest) obj;
        return Objects.equals(body, other.body) && Objects.equals(headers, other.headers) && Objects.equals(host, other.host) && Objects.equals(method, other.method) && Objects.equals(path, other.path) && port == other.port && Objects.equals(query, other.query) && Objects.equals(scheme, other.scheme) && Objects.equals(version, other.version);
    }


    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getRequestLine() + "]";
    }
}
