package io.github.alexisTrejo11.construction.company.modules.notification.shared.persistence;

import io.github.alexisTrejo11.construction.company.modules.notification.shared.domain.Notification;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByRecipientId(Long recipientId, Pageable pageable);

    Page<Notification> findByRecipientIdAndReadAtIsNull(Long recipientId, Pageable pageable);

    Page<Notification> findByRecipientIdAndReadAtIsNotNull(Long recipientId, Pageable pageable);

    Optional<Notification> findByIdAndRecipientId(Long id, Long recipientId);

    long countByRecipientIdAndReadAtIsNull(Long recipientId);

    @Modifying
    @Query("update Notification n set n.readAt = :readAt where n.recipient.id = :recipientId and n.readAt is null")
    int markUnreadAsRead(@Param("recipientId") Long recipientId, @Param("readAt") Instant readAt);
}
