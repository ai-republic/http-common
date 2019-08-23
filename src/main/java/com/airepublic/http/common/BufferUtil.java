package com.airepublic.http.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Utility methods to process a {@link ByteBuffer}.
 * 
 * @author Torsten Oltmanns
 *
 */
public class BufferUtil {
    public static String readLine(final ByteBuffer buffer, final Charset charset) throws IOException {
        byte cur = ' ';

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            while (buffer.hasRemaining()) {
                cur = buffer.get();

                bos.write(cur);

                if (cur == (byte) '\n') {
                    return new String(bos.toByteArray(), charset);
                }
            }
        } catch (final UnsupportedEncodingException e) {
        }

        return null;
    }


    public static String readNextToken(final ByteBuffer buffer, final String token, final Charset charset) throws IOException {
        Objects.nonNull(buffer);
        Objects.nonNull(token);

        byte cur;
        String str = null;

        buffer.mark();

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            while (buffer.hasRemaining()) {
                cur = buffer.get();
                bos.write(cur);

                str = new String(bos.toByteArray(), charset);

                if (str.endsWith(token)) {
                    return str.substring(0, str.length() - token.length());
                }
            }
        } catch (final UnsupportedEncodingException e) {
        }

        return null;
    }


    public static ByteBuffer combineBuffers(final ByteBuffer... buffers) throws IOException {
        return combineBuffers(Stream.of(buffers));
    }


    public static ByteBuffer combineBuffers(final Collection<ByteBuffer> buffers) throws IOException {
        return combineBuffers(buffers.stream());
    }


    public static ByteBuffer combineBuffers(final Stream<ByteBuffer> buffers) throws IOException {

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            buffers.forEach(buffer -> {
                final byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);

                try {
                    bos.write(bytes);
                } catch (final IOException e) {
                    // ignore, cannot happen except when out-of-memory
                }
            });

            return ByteBuffer.wrap(bos.toByteArray());
        } catch (final IOException e) {
            throw e;
        }
    }


    public static ByteBuffer copyRemainingBuffer(final ByteBuffer buffer) {
        final byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return ByteBuffer.wrap(bytes);
    }
}
