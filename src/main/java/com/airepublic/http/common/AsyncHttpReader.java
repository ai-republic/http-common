package com.airepublic.http.common;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * An helper to read HTTP request and responses asynchronously.
 * 
 * @author Torsten Oltmanns
 *
 */
public class AsyncHttpReader {
    private final List<ByteBuffer> requestBuffers = new ArrayList<>();
    private boolean fullyRead = false;
    private HttpRequest httpRequest;
    private HttpResponse httpResponse;


    /**
     * This method will parse the input buffer. If the request/response is fully read it will return
     * true otherwise false if more data is needed.
     * 
     * @param buffer the buffer to process
     * @return if the request is fully read it will return true otherwise false if more data is
     *         needed
     * @throws IOException if reading from the buffer fails
     */
    public boolean receiveBuffer(final ByteBuffer buffer) throws IOException {
        if (!fullyRead) {
            if (buffer.hasRemaining()) {
                requestBuffers.add(BufferUtil.copyRemainingBuffer(buffer));
            }

            // check if finished receiving incoming buffers
            if (buffer.limit() == buffer.capacity()) {
                fullyRead = false;
            } else {
                fullyRead = true;
            }
        } else {
            throw new IOException("Request has already been fully read. Illegal incoming second request!");
        }

        return fullyRead;
    }


    public boolean isFullyRead() {
        return fullyRead;
    }


    /**
     * Parses the received buffers as a {@link HttpRequest}.
     * 
     * @return the {@link HttpRequest}
     * @throws IOException if the buffers do not contain a valid request
     */
    public HttpRequest getHttpRequest() throws IOException {
        if (httpRequest == null) {
            if (fullyRead) {
                final Headers headers = new Headers();
                final String requestLine = parseHeaders(headers);
                final ByteBuffer body = parseBody();
                httpRequest = new HttpRequest(requestLine, headers).withBody(body);
            } else {
                throw new IOException("Illegal state - request has not been fully read!");
            }
        }

        return httpRequest;
    }


    /**
     * Parses the received buffers as a {@link HttpResponse}.
     * 
     * @return the {@link HttpResponse}
     * @throws IOException if the buffers do not contain a valid response
     */
    public HttpResponse getHttpResponse() throws IOException {
        if (httpResponse == null) {
            if (fullyRead) {
                final Headers headers = new Headers();
                final String firstLine = parseHeaders(headers);
                final ByteBuffer body = parseBody();
                final HttpStatus status = parseHttpStatus(firstLine);
                httpResponse = new HttpResponse(status, headers, body);
            } else {
                throw new IOException("Illegal state - request has not been fully read!");
            }
        }

        return httpResponse;
    }


    /**
     * Parses the HTTP status from the first line.
     * 
     * @param firstLine the first line of the raw response
     * @return the {@link HttpStatus}
     */
    private HttpStatus parseHttpStatus(final String firstLine) {
        final String[] split = firstLine.split(" ");
        return HttpStatus.forCode(Integer.valueOf(split[1]));
    }


    /**
     * Parses the received buffers and adds the headers to Headers object.
     * 
     * @param headers the {@link Headers} to populate
     * @return the first line of the request/response
     * @throws IOException if parsing fails
     */
    private String parseHeaders(final Headers headers) throws IOException {
        boolean isFinishedHeaders = false;
        boolean isFirstLine = true;
        String firstLine = null;

        for (final ByteBuffer buffer : requestBuffers) {
            if (!isFinishedHeaders) {
                while (buffer.hasRemaining() && !isFinishedHeaders) {

                    String line = BufferUtil.readLine(buffer, Charset.forName("ASCII"));

                    if (line != null) {
                        line = line.strip();

                        // check if line is empty
                        if (line.isBlank()) {
                            isFinishedHeaders = true;

                            while (line != null && line.isBlank()) {
                                line = BufferUtil.readLine(buffer, Charset.forName("ASCII"));
                            }

                            break;
                        } else {
                            if (isFirstLine) {
                                firstLine = line;
                                isFirstLine = false;
                            } else {
                                final int idx = line.indexOf(':');

                                if (idx != -1) {
                                    final String value = line.substring(idx + 1);
                                    headers.add(line.substring(0, idx), value.strip());
                                } else if (idx == -1) {
                                    throw new IOException("Could not parse header information: " + line);
                                }
                            }
                        }
                    } else {
                        throw new IOException("Could not read header line (null)!");
                    }
                }

                if (isFinishedHeaders) {
                    break;
                }
            }
        }

        return firstLine;
    }


    /**
     * Parses the request/response body.
     * 
     * @return the {@link ByteBuffer} containing only the body
     * @throws IOException if parsing fails
     */
    private ByteBuffer parseBody() throws IOException {
        final ByteBuffer body = BufferUtil.combineBuffers(requestBuffers);
        body.position(0);
        return body;
    }


    /**
     * Clears all received {@link ByteBuffer}s and resets this {@link AsyncHttpReader} to be reused
     * again.
     */
    public void clear() {
        requestBuffers.clear();
        httpRequest = null;
        httpResponse = null;
        fullyRead = false;
    }
}
