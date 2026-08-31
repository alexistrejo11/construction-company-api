package io.github.alexisTrejo11.construction.company.modules.evidence.shared.persistence;

import io.github.alexisTrejo11.construction.company.modules.evidence.shared.domain.Attachment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByEvidenceIdOrderByCreatedAtAsc(Long evidenceId);
}
