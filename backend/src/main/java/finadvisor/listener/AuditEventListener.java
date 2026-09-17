package finadvisor.listener;

import finadvisor.entity.AuditLog;
import finadvisor.events.AuditEvent;
import finadvisor.repository.AuditLogRepository;
import finadvisor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Async
    @EventListener
    public void onAuditEvent(AuditEvent event) {
        AuditLog.AuditLogBuilder builder = AuditLog.builder()
                .action(event.action())
                .details(event.details())
                .ipAddress(event.ipAddress());
        if (event.userId() != null) {
            userRepository.findById(event.userId()).ifPresent(builder::user);
        }
        auditLogRepository.save(builder.build());
    }
}
