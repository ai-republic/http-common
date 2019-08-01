import com.airepublic.microprofile.core.spi.IRequest;
import com.airepublic.microprofile.util.http.common.HttpRequest;

module com.airepublic.http.common {
    exports com.airepublic.http.common;
    exports com.airepublic.http.common.pathmatcher;

    requires jakarta.enterprise.cdi.api;
    requires jakarta.inject;
    requires java.logging;

    provides IRequest with HttpRequest;

    opens com.airepublic.microprofile.util.http.common;
    opens com.airepublic.microprofile.util.http.common.pathmatcher;
}