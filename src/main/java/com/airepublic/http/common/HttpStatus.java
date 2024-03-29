package com.airepublic.http.common;

public enum HttpStatus {

    // --- 1xx Informational ---
    /** {@code 100 1xx Informational} (HTTP/1.1 - RFC 2616) */
    INFORMATIONAL(100),

    /** {@code 100 Continue} (HTTP/1.1 - RFC 2616) */
    CONTINUE(100),
    /** {@code 101 Switching Protocols} (HTTP/1.1 - RFC 2616) */
    SWITCHING_PROTOCOLS(101),
    /** {@code 102 Processing} (WebDAV - RFC 2518) */
    PROCESSING(102),
    /** {@code 103 Early Hints (Early Hints - RFC 8297)} */
    EARLY_HINTS(103),

    // --- 2xx Success ---
    /** {@code 2xx Success} (HTTP/1.0 - RFC 1945) */
    SUCCESS(200),

    /** {@code 200 OK} (HTTP/1.0 - RFC 1945) */
    OK(200),
    /** {@code 201 Created} (HTTP/1.0 - RFC 1945) */
    CREATED(201),
    /** {@code 202 Accepted} (HTTP/1.0 - RFC 1945) */
    ACCEPTED(202),
    /** {@code 203 Non Authoritative Information} (HTTP/1.1 - RFC 2616) */
    NON_AUTHORITATIVE_INFORMATION(203),
    /** {@code 204 No Content} (HTTP/1.0 - RFC 1945) */
    NO_CONTENT(204),
    /** {@code 205 Reset Content} (HTTP/1.1 - RFC 2616) */
    RESET_CONTENT(205),
    /** {@code 206 Partial Content} (HTTP/1.1 - RFC 2616) */
    PARTIAL_CONTENT(206),
    /**
     * {@code 207 Multi-Status} (WebDAV - RFC 2518) or {@code 207 Partial Update OK} (HTTP/1.1 -
     * draft-ietf-http-v11-spec-rev-01?)
     */
    MULTI_STATUS(207),
    /**
     * {@code 208 Already Reported} (WebDAV - RFC 5842, p.30, section 7.1)
     */
    ALREADY_REPORTED(208),
    /**
     * {@code 226 IM Used} (Delta encoding in HTTP - RFC 3229, p. 30, section 10.4.1)
     */
    IM_USED(226),

    // --- 3xx Redirection ---
    /** {@code 3xx Redirection} (HTTP/1.1 - RFC 2616) */
    REDIRECTION(300),

    /** {@code 300 Mutliple Choices} (HTTP/1.1 - RFC 2616) */
    MULTIPLE_CHOICES(300),
    /** {@code 301 Moved Permanently} (HTTP/1.0 - RFC 1945) */
    MOVED_PERMANENTLY(301),
    /** {@code 302 Moved Temporarily} (Sometimes {@code Found}) (HTTP/1.0 - RFC 1945) */
    MOVED_TEMPORARILY(302),
    /** {@code 303 See Other} (HTTP/1.1 - RFC 2616) */
    SEE_OTHER(303),
    /** {@code 304 Not Modified} (HTTP/1.0 - RFC 1945) */
    NOT_MODIFIED(304),
    /** {@code 305 Use Proxy} (HTTP/1.1 - RFC 2616) */
    USE_PROXY(305),
    /** {@code 307 Temporary Redirect} (HTTP/1.1 - RFC 2616) */
    TEMPORARY_REDIRECT(307),

    /** {@code 308 Permanent Redirect} (HTTP/1.1 - RFC 7538) */
    PERMANENT_REDIRECT(308),

    // --- 4xx Client Error ---
    /** {@code 4xx Client Error} (HTTP/1.1 - RFC 2616) */
    CLIENT_ERROR(400),

    /** {@code 400 Bad Request} (HTTP/1.1 - RFC 2616) */
    BAD_REQUEST(400),
    /** {@code 401 Unauthorized} (HTTP/1.0 - RFC 1945) */
    UNAUTHORIZED(401),
    /** {@code 402 Payment Required} (HTTP/1.1 - RFC 2616) */
    PAYMENT_REQUIRED(402),
    /** {@code 403 Forbidden} (HTTP/1.0 - RFC 1945) */
    FORBIDDEN(403),
    /** {@code 404 Not Found} (HTTP/1.0 - RFC 1945) */
    NOT_FOUND(404),
    /** {@code 405 Method Not Allowed} (HTTP/1.1 - RFC 2616) */
    METHOD_NOT_ALLOWED(405),
    /** {@code 406 Not Acceptable} (HTTP/1.1 - RFC 2616) */
    NOT_ACCEPTABLE(406),
    /** {@code 407 Proxy Authentication Required} (HTTP/1.1 - RFC 2616) */
    PROXY_AUTHENTICATION_REQUIRED(407),
    /** {@code 408 Request Timeout} (HTTP/1.1 - RFC 2616) */
    REQUEST_TIMEOUT(408),
    /** {@code 409 Conflict} (HTTP/1.1 - RFC 2616) */
    CONFLICT(409),
    /** {@code 410 Gone} (HTTP/1.1 - RFC 2616) */
    GONE(410),
    /** {@code 411 Length Required} (HTTP/1.1 - RFC 2616) */
    LENGTH_REQUIRED(411),
    /** {@code 412 Precondition Failed} (HTTP/1.1 - RFC 2616) */
    PRECONDITION_FAILED(412),
    /** {@code 413 Request Entity Too Large} (HTTP/1.1 - RFC 2616) */
    REQUEST_TOO_LONG(413),
    /** {@code 414 Request-URI Too Long} (HTTP/1.1 - RFC 2616) */
    REQUEST_URI_TOO_LONG(414),
    /** {@code 415 Unsupported Media Type} (HTTP/1.1 - RFC 2616) */
    UNSUPPORTED_MEDIA_TYPE(415),
    /** {@code 416 Requested Range Not Satisfiable} (HTTP/1.1 - RFC 2616) */
    REQUESTED_RANGE_NOT_SATISFIABLE(416),
    /** {@code 417 Expectation Failed} (HTTP/1.1 - RFC 2616) */
    EXPECTATION_FAILED(417),
    /** {@code 421 Misdirected Request} (HTTP/2 - RFC 7540) */
    MISDIRECTED_REQUEST(421),

    /**
     * Static constant for a 418 error. {@code 418 Unprocessable Entity} (WebDAV drafts?) or
     * {@code 418 Reauthentication Required} (HTTP/1.1 drafts?)
     */
    // not used
    // UNPROCESSABLE_ENTITY(418),

    /**
     * Static constant for a 419 error. {@code 419 Insufficient Space on Resource} (WebDAV -
     * draft-ietf-webdav-protocol-05?) or {@code 419 Proxy Reauthentication Required} (HTTP/1.1
     * drafts?)
     */
    INSUFFICIENT_SPACE_ON_RESOURCE(419),
    /**
     * Static constant for a 420 error. {@code 420 Method Failure} (WebDAV -
     * draft-ietf-webdav-protocol-05?)
     */
    METHOD_FAILURE(420),
    /** {@code 422 Unprocessable Entity} (WebDAV - RFC 2518) */
    UNPROCESSABLE_ENTITY(422),
    /** {@code 423 Locked} (WebDAV - RFC 2518) */
    LOCKED(423),
    /** {@code 424 Failed Dependency} (WebDAV - RFC 2518) */
    FAILED_DEPENDENCY(424),
    /** {@code 428 Precondition Required} (Additional HTTP Status Codes - RFC 6585) */
    PRECONDITION_REQUIRED(428),
    /** {@code 429 Too Many Requests} (Additional HTTP Status Codes - RFC 6585) */
    TOO_MANY_REQUESTS(429),
    /** {@code 431 Request Header Fields Too Large} (Additional HTTP Status Codes - RFC 6585) */
    REQUEST_HEADER_FIELDS_TOO_LARGE(431),
    /** {@code 451 Unavailable For Legal Reasons} (Legal Obstacles - RFC 7725) */
    UNAVAILABLE_FOR_LEGAL_REASONS(451),

    // --- 5xx Server Error ---
    /** {@code 500 Server Error} (HTTP/1.0 - RFC 1945) */
    SERVER_ERROR(500),

    /** {@code 500 Internal Server Error} (HTTP/1.0 - RFC 1945) */
    INTERNAL_SERVER_ERROR(500),
    /** {@code 501 Not Implemented} (HTTP/1.0 - RFC 1945) */
    NOT_IMPLEMENTED(501),
    /** {@code 502 Bad Gateway} (HTTP/1.0 - RFC 1945) */
    BAD_GATEWAY(502),
    /** {@code 503 Service Unavailable} (HTTP/1.0 - RFC 1945) */
    SERVICE_UNAVAILABLE(503),
    /** {@code 504 Gateway Timeout} (HTTP/1.1 - RFC 2616) */
    GATEWAY_TIMEOUT(504),
    /** {@code 505 HTTP Version Not Supported} (HTTP/1.1 - RFC 2616) */
    HTTP_VERSION_NOT_SUPPORTED(505),
    /** {@code 506 Variant Also Negotiates} ( Transparent Content Negotiation - RFC2295) */
    VARIANT_ALSO_NEGOTIATES(506),
    /** {@code 507 Insufficient Storage} (WebDAV - RFC 2518) */
    INSUFFICIENT_STORAGE(507),

    /**
     * {@code 508 Loop Detected} (WebDAV - RFC 5842, p.33, section 7.2)
     */
    LOOP_DETECTED(508),

    /**
     * {@code 510 Not Extended} (An HTTP Extension Framework - RFC 2774, p. 10, section 7)
     */
    NOT_EXTENDED(510),

    /** {@code  511 Network Authentication Required} (Additional HTTP Status Codes - RFC 6585) */
    NETWORK_AUTHENTICATION_REQUIRED(511);

    int code;


    HttpStatus(final int code) {
        this.code = code;
    }


    public int code() {
        return code;
    }


    public static HttpStatus forCode(final int code) {
        for (final HttpStatus status : HttpStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }

        throw new IllegalArgumentException("HttpStatus with code " + code + " does not exist!");
    }
}
