package com.ofg.infrastructure.camel;

import com.google.common.collect.Iterables;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.sleuth.IdGenerator;
import org.springframework.cloud.sleuth.MilliSpan;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Trace;

import java.lang.invoke.MethodHandles;

import static com.ofg.infrastructure.correlationid.CorrelationIdHolder.CORRELATION_ID_HEADER;
import static com.ofg.infrastructure.correlationid.CorrelationIdHolder.OLD_CORRELATION_ID_HEADER;
import static org.springframework.util.StringUtils.hasText;

/**
 * Interceptor class that ensures the correlationId header is present in {@Exchange}.
 */
public class CorrelationIdInterceptor implements Processor {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final IdGenerator idGenerator;
    private final Trace trace;

    public CorrelationIdInterceptor(IdGenerator idGenerator, Trace trace) {
        this.idGenerator = idGenerator;
        this.trace = trace;
    }

    /**
     * Ensures correlationId header is set in incoming message (if is missing a new correlationId is created and set).
     *
     * @param exchange Camel's container holding received message
     * @throws Exception if an internal processing error has occurred
     */
    @Override
    public void process(Exchange exchange) throws Exception {
        Span span = getCorrelationId(exchange);
        trace.continueSpan(span);
        setCorrelationIdHeaderIfMissing(exchange, span);
    }

    private Span getCorrelationId(Exchange exchange) {
        String traceId = (String) exchange.getIn().getHeader(CORRELATION_ID_HEADER);
        String oldTraceId = (String) exchange.getIn().getHeader(OLD_CORRELATION_ID_HEADER);
        String spanId = (String) exchange.getIn().getHeader(Trace.SPAN_ID_NAME);
        String notSampledName = (String) exchange.getIn().getHeader(Trace.SPAN_NAME_NAME);
        String parentId = (String) exchange.getIn().getHeader(Trace.PARENT_ID_NAME);
        String processID = (String) exchange.getIn().getHeader(Trace.PROCESS_ID_NAME);
        if (!hasText(traceId) && ! hasText(oldTraceId)) {
            log.debug("No correlationId has been set in request inbound message. Creating new one.");
            traceId = idGenerator.create();
        }
        if (!hasText(spanId)) {
            log.debug("No spanId has been set in request inbound message. Creating new one.");
            spanId = idGenerator.create();
        }
        return MilliSpan.builder().spanId(spanId).traceId(firstNonNull(oldTraceId, traceId))
                .name(notSampledName).parent(parentId).processId(processID).build();
    }

    private String firstNonNull(String first, String second) {
        if (hasText(first)) {
            return first;
        }
        return second;
    }

    private void setCorrelationIdHeaderIfMissing(Exchange exchange, Span span) {
        Message inboundMessage = exchange.getIn();
        if (!inboundMessage.getHeaders().containsKey(OLD_CORRELATION_ID_HEADER)) {
            log.debug("Setting correlationId [{}] in header of inbound message", span.getTraceId());
            inboundMessage.setHeader(Trace.SPAN_ID_NAME, span.getSpanId());
            inboundMessage.setHeader(CORRELATION_ID_HEADER, span.getTraceId());
            inboundMessage.setHeader(OLD_CORRELATION_ID_HEADER, span.getTraceId());
            inboundMessage.setHeader(Trace.SPAN_NAME_NAME, span.getName());
            inboundMessage.setHeader(Trace.PARENT_ID_NAME, Iterables.getFirst(span.getParents(), null));
            inboundMessage.setHeader(Trace.PROCESS_ID_NAME, span.getProcessId());
        }

    }
}
