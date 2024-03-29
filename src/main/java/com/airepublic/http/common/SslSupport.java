package com.airepublic.http.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * Class to support creating {@link SSLContext} and {@link SSLEngine}.
 * 
 * @author Torsten Oltmanns
 *
 */
public class SslSupport {
    private final static Logger LOG = Logger.getLogger(SslSupport.class.getName());
    private final static ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static int applicationBufferSize = 16 * 1024;
    private static int packetBufferSize = 16 * 1024;


    /**
     * Creates a {@link SSLContext} in client mode.
     * 
     * @return the {@link SSLContext}
     * @throws IOException if creating the {@link SSLContext} fails
     */
    public static SSLContext createClientSSLContext() throws IOException {
        return createSSLContext(true, null, null, null, null);
    }


    /**
     * Creates a {@link SSLContext} in server mode.
     *
     * @param keystoreFile the path to the keystore
     * @param keystorePassword the password for the keystore
     * @param truststoreFile the path to the truststore
     * @param truststorePassword the password for the truststore
     * @return the {@link SSLContext}
     * @throws IOException if creating the {@link SSLContext} fails
     */
    public static SSLContext createServerSSLContext(final String keystoreFile, final String keystorePassword, final String truststoreFile, final String truststorePassword) throws IOException {
        return createSSLContext(false, keystoreFile, keystorePassword, truststoreFile, truststorePassword);
    }


    /**
     * Creates a {@link SSLContext} in client or server mode.
     *
     * @param isClient flag whether to create the {@link SSLContext} in client or server mode.
     * @param keystoreFile the path to the keystore
     * @param keystorePassword the password for the keystore
     * @param truststoreFile the path to the truststore
     * @param truststorePassword the password for the truststore
     * @return the {@link SSLContext}
     * @throws IOException if creating the {@link SSLContext} fails
     */
    static SSLContext createSSLContext(final boolean isClient, final String keystoreFile, final String keystorePassword, final String truststoreFile, final String truststorePassword) throws IOException {
        final SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("SSL");

            try {
                if (isClient) {
                    // create client context
                    sslContext.init(null, null, null);
                } else {
                    // create server context
                    final KeyManager[] keyManagers = createKeyManagers(keystoreFile, truststorePassword, keystorePassword);
                    final TrustManager[] trustManagers = createTrustManagers(truststoreFile, truststorePassword);
                    sslContext.init(keyManagers, trustManagers, new SecureRandom());
                }
            } catch (final Exception e) {
                throw new IOException("Could not get initialize SSLContext!", e);
            }

            final SSLSession dummySession = sslContext.createSSLEngine().getSession();
            setApplicationBufferSize(dummySession.getApplicationBufferSize());
            setPacketBufferSize(dummySession.getPacketBufferSize());
            dummySession.invalidate();

        } catch (final Exception e) {
            throw new IOException("Could not get instance of SSLContext!", e);
        }

        return sslContext;
    }


    /**
     * Creates the key managers required to initiate the {@link SSLContext}, using a JKS keystore as
     * an input.
     *
     * @param filepath - the path to the JKS keystore.
     * @param keystorePassword - the keystore's password.
     * @param keyPassword - the key's passsword.
     * @return {@link KeyManager} array that will be used to initiate the {@link SSLContext}.
     * @throws Exception if keymanagers could not be created
     */
    public static KeyManager[] createKeyManagers(final String filepath, final String keystorePassword, final String keyPassword) throws Exception {
        final KeyStore keyStore = KeyStore.getInstance("JKS");
        final InputStream keyStoreIS = new FileInputStream(filepath);

        try {
            keyStore.load(keyStoreIS, keystorePassword.toCharArray());
        } finally {
            if (keyStoreIS != null) {
                keyStoreIS.close();
            }
        }

        final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyPassword.toCharArray());
        return kmf.getKeyManagers();
    }


    /**
     * Creates the trust managers required to initiate the {@link SSLContext}, using a JKS keystore
     * as an input.
     *
     * @param filepath - the path to the JKS keystore.
     * @param keystorePassword - the keystore's password.
     * @return {@link TrustManager} array, that will be used to initiate the {@link SSLContext}.
     * @throws Exception if trustmanagers could not be created
     */
    public static TrustManager[] createTrustManagers(final String filepath, final String keystorePassword) throws Exception {
        final KeyStore trustStore = KeyStore.getInstance("JKS");
        final InputStream trustStoreIS = new FileInputStream(filepath);

        try {
            trustStore.load(trustStoreIS, keystorePassword.toCharArray());
        } finally {
            if (trustStoreIS != null) {
                trustStoreIS.close();
            }
        }

        final TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustFactory.init(trustStore);
        return trustFactory.getTrustManagers();
    }


    public static ByteBuffer enlargeApplicationBuffer(final SSLEngine engine, final ByteBuffer buffer) {
        return enlargeBuffer(buffer, engine.getSession().getApplicationBufferSize());
    }


    /**
     * Implements the handshake protocol between two peers, required for the establishment of the
     * SSL/TLS connection. During the handshake, encryption configuration information - such as the
     * list of available cipher suites - will be exchanged and if the handshake is successful will
     * lead to an established SSL/TLS session.
     *
     * <p>
     * A typical handshake will usually contain the following steps:
     * </p>
     * <ul>
     * <li>1. wrap: ClientHello</li>
     * <li>2. unwrap: ServerHello/Cert/ServerHelloDone</li>
     * <li>3. wrap: ClientKeyExchange</li>
     * <li>4. wrap: ChangeCipherSpec</li>
     * <li>5. wrap: Finished</li>
     * <li>6. unwrap: ChangeCipherSpec</li>
     * <li>7. unwrap: Finished</li>
     * </ul>
     * <p>
     * Handshake is also used during the end of the session, in order to properly close the
     * connection between the two peers. A proper connection close will typically include the one
     * peer sending a CLOSE message to another, and then wait for the other's CLOSE message to close
     * the transport link. The other peer from his perspective would read a CLOSE message from his
     * peer and then enter the handshake procedure to send his own CLOSE message as well.
     * </p>
     * 
     * @param socketChannel - the socket channel that connects the two peers.
     * @param engine - the engine that will be used for encryption/decryption of the data exchanged
     *        with the other peer.
     * @return True if the connection handshake was successful or false if an error occurred.
     * @throws IOException - if an error occurs during read/write to the socket channel.
     */
    public static boolean doHandshake(final SocketChannel socketChannel, final SSLEngine engine) throws IOException {

        LOG.fine("Performing SSL handshake...");
        ByteBuffer packetBuffer = ByteBuffer.allocate(packetBufferSize);
        ByteBuffer peerPacketBuffer = ByteBuffer.allocate(packetBufferSize);

        SSLEngineResult result;
        HandshakeStatus handshakeStatus;

        // NioSslPeer's fields myAppData and peerAppData are supposed to be large enough to hold all
        // message data the peer
        // will send and expects to receive from the other peer respectively. Since the messages to
        // be exchanged will usually be less
        // than 16KB long the capacity of these fields should also be smaller. Here we initialize
        // these two local buffers
        // to be used for the handshake, while keeping client's buffers at the same size.
        final int appBufferSize = engine.getSession().getApplicationBufferSize();
        final ByteBuffer myAppData = ByteBuffer.allocate(appBufferSize);
        ByteBuffer peerAppData = ByteBuffer.allocate(appBufferSize);
        packetBuffer.clear();
        peerPacketBuffer.clear();

        handshakeStatus = engine.getHandshakeStatus();

        while (handshakeStatus != SSLEngineResult.HandshakeStatus.FINISHED && handshakeStatus != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            switch (handshakeStatus) {
                case NEED_UNWRAP:
                    if (socketChannel.read(peerPacketBuffer) < 0) {
                        if (engine.isInboundDone() && engine.isOutboundDone()) {
                            return false;
                        }
                        try {
                            engine.closeInbound();
                        } catch (final SSLException e) {
                            LOG.log(Level.SEVERE, "This engine was forced to close inbound, without having received the proper SSL/TLS close notification message from the peer, due to end of stream.", e);
                        }
                        engine.closeOutbound();
                        // After closeOutbound the engine will be set to WRAP state, in order to try
                        // to send a close message to the client.
                        handshakeStatus = engine.getHandshakeStatus();
                        break;
                    }

                    peerPacketBuffer.flip();

                    try {
                        result = engine.unwrap(peerPacketBuffer, peerAppData);
                        peerPacketBuffer.compact();
                        handshakeStatus = result.getHandshakeStatus();
                    } catch (final SSLException e) {
                        LOG.log(Level.SEVERE, "A problem was encountered while processing the data that caused the SSLEngine to abort. Will try to properly close connection...", e);
                        engine.closeOutbound();
                        handshakeStatus = engine.getHandshakeStatus();
                        break;
                    }

                    switch (result.getStatus()) {
                        case OK:
                        break;
                        case BUFFER_OVERFLOW:
                            // Will occur when peerAppData's capacity is smaller than the data
                            // derived from peerNetData's unwrap.
                            peerAppData = SslSupport.enlargeApplicationBuffer(engine, peerAppData);
                        break;
                        case BUFFER_UNDERFLOW:
                            // Will occur either when no data was read from the peer or when the
                            // peerNetData buffer was too small to hold all peer's data.
                            peerPacketBuffer = SslSupport.handleBufferUnderflow(engine, peerPacketBuffer);
                        break;
                        case CLOSED:
                            if (engine.isOutboundDone()) {
                                return false;
                            } else {
                                engine.closeOutbound();
                                handshakeStatus = engine.getHandshakeStatus();
                                break;
                            }
                        default:
                            throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                    }
                break;

                case NEED_WRAP:
                    packetBuffer.clear();

                    try {
                        result = engine.wrap(myAppData, packetBuffer);
                        handshakeStatus = result.getHandshakeStatus();
                    } catch (final SSLException e) {
                        LOG.log(Level.SEVERE, "A problem was encountered while processing the data that caused the SSLEngine to abort. Will try to properly close connection...", e);
                        engine.closeOutbound();
                        handshakeStatus = engine.getHandshakeStatus();
                        break;
                    }

                    switch (result.getStatus()) {
                        case OK:
                            packetBuffer.flip();
                            while (packetBuffer.hasRemaining()) {
                                socketChannel.write(packetBuffer);
                            }
                        break;

                        case BUFFER_OVERFLOW:
                            // Will occur if there is not enough space in myNetData buffer to write
                            // all the data that would be generated by the method wrap.
                            // Since myNetData is set to session's packet size we should not get to
                            // this point because SSLEngine is supposed
                            // to produce messages smaller or equal to that, but a general handling
                            // would be the following:
                            packetBuffer = SslSupport.enlargePacketBuffer(engine, packetBuffer);
                        break;

                        case BUFFER_UNDERFLOW:
                            throw new SSLException("Buffer underflow occured after a wrap. I don't think we should ever get here.");

                        case CLOSED:
                            try {
                                packetBuffer.flip();
                                while (packetBuffer.hasRemaining()) {
                                    socketChannel.write(packetBuffer);
                                }
                                // At this point the handshake status will probably be NEED_UNWRAP
                                // so we make sure that peerNetData is clear to read.
                                peerPacketBuffer.clear();
                            } catch (final Exception e) {
                                LOG.log(Level.SEVERE, "Failed to send server's CLOSE message due to socket channel's failure.");
                                handshakeStatus = engine.getHandshakeStatus();
                            }
                        break;

                        default:
                            throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                    }
                break;

                case NEED_TASK:
                    Runnable task;

                    while ((task = engine.getDelegatedTask()) != null) {
                        executorService.execute(task);
                    }

                    handshakeStatus = engine.getHandshakeStatus();
                break;

                case FINISHED:
                break;

                case NOT_HANDSHAKING:
                break;

                default:
                    throw new IllegalStateException("Invalid SSL status: " + handshakeStatus);
            }
        }

        return true;
    }


    /**
     * Compares <code>sessionProposedCapacity</code> with buffer's capacity. If buffer's capacity is
     * smaller, returns a buffer with the proposed capacity. If it's equal or larger, returns a
     * buffer with capacity twice the size of the initial one.
     *
     * @param buffer - the buffer to be enlarged.
     * @param sessionProposedCapacity - the minimum size of the new buffer, proposed by
     *        {@link SSLSession}.
     * @return A new buffer with a larger capacity.
     */
    public static ByteBuffer enlargeBuffer(ByteBuffer buffer, final int sessionProposedCapacity) {
        if (sessionProposedCapacity > buffer.capacity()) {
            buffer = ByteBuffer.allocate(sessionProposedCapacity);
        } else {
            buffer = ByteBuffer.allocate(buffer.capacity() * 2);
        }

        return buffer;
    }


    public static ByteBuffer enlargePacketBuffer(final SSLEngine engine, final ByteBuffer buffer) {
        return enlargeBuffer(buffer, engine.getSession().getPacketBufferSize());
    }


    /**
     * Handles {@link SSLEngineResult.Status#BUFFER_UNDERFLOW}. Will check if the buffer is already
     * filled, and if there is no space problem will return the same buffer, so the client tries to
     * read again. If the buffer is already filled will try to enlarge the buffer either to
     * session's proposed size or to a larger capacity. A buffer underflow can happen only after an
     * unwrap, so the buffer will always be a peerNetData buffer.
     *
     * @param buffer - will always be peerNetData buffer.
     * @param engine - the engine used for encryption/decryption of the data exchanged between the
     *        two peers.
     * @return The same buffer if there is no space problem or a new buffer with the same data but
     *         more space.
     */
    public static ByteBuffer handleBufferUnderflow(final SSLEngine engine, final ByteBuffer buffer) {
        if (engine.getSession().getPacketBufferSize() < buffer.limit()) {
            return buffer;
        } else {
            final ByteBuffer replaceBuffer = enlargePacketBuffer(engine, buffer);
            buffer.flip();
            replaceBuffer.put(buffer);
            return replaceBuffer;
        }
    }


    /**
     * This method should be called when this peer wants to explicitly close the connection or when
     * a close message has arrived from the other peer, in order to provide an orderly shutdown.
     * <p>
     * It first calls {@link SSLEngine#closeOutbound()} which prepares this peer to send its own
     * close message and sets {@link SSLEngine} to the <code>NEED_WRAP</code> state. Then, it
     * delegates the exchange of close messages to the handshake method and finally, it closes
     * socket channel.
     * </p>
     * 
     * @param socketChannel - the transport link used between the two peers.
     * @param engine - the engine used for encryption/decryption of the data exchanged between the
     *        two peers.
     * @throws IOException if an I/O error occurs to the socket channel.
     */
    public static void closeConnection(final SocketChannel socketChannel, final SSLEngine engine) throws IOException {
        engine.closeOutbound();
        doHandshake(socketChannel, engine);
        socketChannel.close();
    }


    /**
     * Gets the packet buffer size.
     * 
     * @return the packet buffer size
     */
    public static int getPacketBufferSize() {
        return packetBufferSize;
    }


    /**
     * Sets the packet buffer size.
     * 
     * @param size the packet buffer size
     */
    public static void setPacketBufferSize(final int size) {
        packetBufferSize = size;
    }


    /**
     * Gets the application buffer size.
     * 
     * @return the application buffer size
     */
    public static int getApplicationBufferSize() {
        return applicationBufferSize;
    }


    /**
     * Sets the application buffer size.
     * 
     * @param size the application buffer size
     */
    public static void setApplicationBufferSize(final int size) {
        applicationBufferSize = size;
    }


    /**
     * Unwraps the content of the {@link ByteBuffer} using the {@link SSLEngine}. If the connection
     * is closed by the peer it will also close the channel.
     * 
     * @param sslEngine the {@link SSLEngine}
     * @param channel the {@link SocketChannel}
     * @param buffer the {@link ByteBuffer}
     * @return the unwrapped {@link ByteBuffer}
     * @throws IOException if decryption fails
     */
    public static ByteBuffer unwrap(final SSLEngine sslEngine, final SocketChannel channel, final ByteBuffer buffer) throws IOException {
        if (sslEngine == null) {
            return buffer;
        }

        final ByteBuffer unwrapBuffer = ByteBuffer.allocate(buffer.capacity());

        final SSLEngineResult result = sslEngine.unwrap(buffer, unwrapBuffer);

        switch (result.getStatus()) {
            case OK:
                unwrapBuffer.flip();
                return unwrapBuffer;
            case BUFFER_OVERFLOW:
                return SslSupport.enlargeApplicationBuffer(sslEngine, unwrapBuffer);
            case BUFFER_UNDERFLOW:
                return SslSupport.handleBufferUnderflow(sslEngine, buffer);
            case CLOSED:
                LOG.fine("Closing SSL connection...");
                closeConnection(channel, sslEngine);
                return null;
            default:
                throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
        }
    }


    /**
     * Wraps the contents of the {@link ByteBuffer}s using the {@link SSLEngine}. If the connection
     * is closed by the peer it will also close the channel.
     * 
     * @param sslEngine the {@link SSLEngine}
     * @param channel the {@link SocketChannel}
     * @param buffers the {@link ByteBuffer}s
     * @return the unwrapped {@link ByteBuffer}
     * @throws IOException if encryption fails
     */
    public static ByteBuffer[] wrap(final SSLEngine sslEngine, final SocketChannel channel, final ByteBuffer... buffers) throws IOException {
        if (sslEngine == null) {
            return buffers;
        }

        final ByteBuffer[] wrappedBuffers = new ByteBuffer[buffers.length];

        for (int i = 0; i < buffers.length; i++) {
            final ByteBuffer buffer = buffers[i];
            boolean retry = false;

            do {
                ByteBuffer wrappedBuffer = ByteBuffer.allocate(SslSupport.getPacketBufferSize());
                final SSLEngineResult result = sslEngine.wrap(buffer, wrappedBuffer);
                retry = false;

                switch (result.getStatus()) {
                    case OK:
                        wrappedBuffer.flip();
                        wrappedBuffers[i] = wrappedBuffer;
                    break;
                    case BUFFER_OVERFLOW:
                        wrappedBuffer = SslSupport.enlargePacketBuffer(sslEngine, wrappedBuffer);
                        retry = true;
                    break;
                    case BUFFER_UNDERFLOW:
                        throw new SSLException("Buffer underflow occured after a wrap. I don't think we should ever get here.");
                    case CLOSED:
                        LOG.fine("Closing SSL connection...");
                        closeConnection(channel, sslEngine);
                        return null;
                    default:
                        throw new IllegalStateException("Invalid SSL status: " + result.getStatus());
                }
            } while (retry);
        }

        return wrappedBuffers;
    }


    /**
     * Performs a SSL handshake in client mode.
     * 
     * @param sslContext the {@link SSLContext} in client mode
     * @param channel the {@link SocketChannel}
     * @param uri the {@link URI} to connect to
     * @return the {@link SSLEngine} created for the connection
     * @throws IOException if handshaking fails
     */
    public static SSLEngine clientSSLHandshake(final SSLContext sslContext, final SocketChannel channel, final URI uri) throws IOException {
        SSLEngine sslEngine;
        boolean success = false;

        try {
            final String host = uri.getHost();
            Integer port = uri.getPort();

            if (port == null || port == -1) {
                port = 443;
            }

            if (host == null || port == null) {
                throw new IOException("Peer host and port not specified for client SSL connection!");
            }

            sslEngine = sslContext.createSSLEngine(host, port);

            sslEngine.setUseClientMode(true);
            sslEngine.beginHandshake();

            success = SslSupport.doHandshake(channel, sslEngine);
        } catch (final Exception e) {
            throw new IOException("Could not perform SSL handshake!", e);
        }

        if (!success) {
            channel.close();
            throw new IOException("Connection closed due to handshake failure.");
        }

        return sslEngine;
    }


    /**
     * Performs a SSL handshake in server mode.
     * 
     * @param sslContext the {@link SSLContext} in server mode
     * @param channel the {@link SocketChannel}
     * @return the {@link SSLEngine} created for the connection
     * @throws IOException if handshaking fails
     */
    public static SSLEngine serverSSLHandshake(final SSLContext sslContext, final SocketChannel channel) throws IOException {
        SSLEngine sslEngine;
        boolean success = false;

        try {
            sslEngine = sslContext.createSSLEngine();

            sslEngine.setUseClientMode(false);
            sslEngine.beginHandshake();

            success = SslSupport.doHandshake(channel, sslEngine);
        } catch (final Exception e) {
            throw new IOException("Could not perform SSL handshake!", e);
        }

        if (!success) {
            channel.close();
            throw new IOException("Connection closed due to handshake failure.");
        }

        return sslEngine;
    }

}
