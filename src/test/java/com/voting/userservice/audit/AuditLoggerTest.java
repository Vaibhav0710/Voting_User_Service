package com.voting.userservice.audit;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

class AuditLoggerTest {

    private final Logger auditLog = (Logger) LoggerFactory.getLogger(AuditLogger.LOGGER_NAME);
    private final ListAppender<ILoggingEvent> captured = new ListAppender<>();
    private final AuditLogger auditLogger = new AuditLogger();

    @BeforeEach
    void attach() {
        captured.start();
        auditLog.addAppender(captured);
    }

    @AfterEach
    void detach() {
        auditLog.detachAppender(captured);
    }

    @Test
    void record_writesOneKeyValueLineToTheAuditLogger() {
        auditLogger.record("LOGIN", "asha", "user:42", "SUCCESS");

        assertThat(captured.list).hasSize(1);
        assertThat(captured.list.get(0).getFormattedMessage())
                .isEqualTo("action=LOGIN actor=asha target=user:42 outcome=SUCCESS");
    }

    @Test
    void record_carriesFieldsAsStructuredArgumentsForJsonLogs() {
        auditLogger.record("USER_REGISTERED", "asha", "user:42", "SUCCESS");

        assertThat(captured.list.get(0).getArgumentArray()).hasSize(4);
    }
}
