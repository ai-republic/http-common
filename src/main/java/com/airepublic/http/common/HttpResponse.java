package com.airepublic.http.common;

import java.nio.ByteBuffer;

/**
 * The basic HTTP response.
 * 
 * @author Torsten Oltmanns
 *
 */
public class HttpResponse {
    private String scheme = "http";
    private String version = "1.1";
    private HttpStatus status;
    private Headers headers;
    private ByteBuffer body;


    /**
     * Constructor.
     */
    public HttpResponse() {
    }


    /**
     * Constructor.
     * 
     * @param status the response {@link HttpStatus}
     */
    public HttpResponse(final HttpStatus status) {
        this(status, new Headers(), null);
    }


    /**
     * Constructor.
     * 
     * @param status the response {@link HttpStatus}
     * @param headers the response {@link Headers}
     */
    public HttpResponse(final HttpStatus status, final Headers headers) {
        this(status, headers, null);
    }


    /**
     * Constructor.
     * 
     * @param status the response {@link HttpStatus}
     * @param headers the response {@link Headers}
     * @param body the response body as {@link ByteBuffer}
     */
    public HttpResponse(final HttpStatus status, final Headers headers, final ByteBuffer body) {
        this.status = status;
        this.headers = headers;
        this.body = body;
    }


    /**
     * Gets the response scheme.
     * 
     * @return the scheme
     */
    public String getScheme() {
        return scheme;
    }


    /**
     * Sets the response scheme.
     * 
     * @param scheme the scheme to set
     */
    public void setScheme(final String scheme) {
        this.scheme = scheme;
    }


    /**
     * Gets the HTTP version.
     * 
     * @return the version
     */
    public String getVersion() {
        return version;
    }


    /**
     * Sets the HTTP version.
     * 
     * @param version the version to set
     */
    public void setVersion(final String version) {
        this.version = version;
    }


    /**
     * Gets the HTTP status.
     * 
     * @return the status
     */
    public HttpStatus getStatus() {
        return status;
    }


    /**
     * Sets the HTTP status.
     * 
     * @param status the status to set
     */
    public void setStatus(final HttpStatus status) {
        this.status = status;
    }


    /**
     * Gets the HTTP response headers.
     * 
     * @return the headers
     */
    public Headers getHeaders() {
        return headers;
    }


    /**
     * Sets the HTTP response headers.
     * 
     * @param headers the headers to set
     */
    public void setHeaders(final Headers headers) {
        this.headers = headers;
    }


    /**
     * Gets the HTTP response body.
     * 
     * @return the body
     */
    public ByteBuffer getBody() {
        return body;
    }


    /**
     * Sets the HTTP response body.
     * 
     * @param body the body to set
     */
    public void setBody(final ByteBuffer body) {
        this.body = body;
    }


    /**
     * Gets the {@link Headers} as buffer.
     * 
     * @return the {@link Headers} as {@link ByteBuffer}
     */
    public ByteBuffer getHeaderBuffer() {
        if (headers != null && scheme != null && status != null) {
            final StringBuffer str = new StringBuffer();
            str.append(scheme.toUpperCase() + "/" + version + " " + status.code() + " " + status.name() + "\r\n");

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

        return null;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (body == null ? 0 : body.hashCode());
        result = prime * result + (headers == null ? 0 : headers.hashCode());
        result = prime * result + (status == null ? 0 : status.hashCode());
        return result;
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
        final HttpResponse other = (HttpResponse) obj;
        if (body == null) {
            if (other.body != null) {
                return false;
            }
        } else if (!body.equals(other.body)) {
            return false;
        }
        if (headers == null) {
            if (other.headers != null) {
                return false;
            }
        } else if (!headers.equals(other.headers)) {
            return false;
        }
        if (status != other.status) {
            return false;
        }
        return true;
    }


    @Override
    public String toString() {
        return "HttpResponse [status=" + status + ", headers=" + headers + ", body=" + body + "]";
    }

}
