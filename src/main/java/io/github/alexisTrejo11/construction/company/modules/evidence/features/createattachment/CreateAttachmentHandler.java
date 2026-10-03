package io.github.alexisTrejo11.construction.company.modules.evidence.features.createattachment;

import io.github.alexisTrejo11.construction.company.modules.evidence.shared.access.EvidenceAccess;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.domain.Attachment;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.dto.AttachmentResponse;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.mapper.EvidenceResponseMapper;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.persistence.AttachmentRepository;
import io.github.alexisTrejo11.construction.company.modules.evidence.shared.storage.FileStorage;
import io.github.alexisTrejo11.construction.company.shared.Result;
import io.github.alexisTrejo11.construction.company.shared.authorization.Permission;
import io.github.alexisTrejo11.construction.company.shared.dto.auth.UserContext;
import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CreateAttachmentHandler {
    private final EvidenceAccess evidenceAccess;
    private final AttachmentRepository attachmentRepository;
    private final EvidenceResponseMapper mapper;
    private final FileStorage fileStorage;

    @Value("${file.upload.max-size-bytes}")
    private long maxSizeBytes;

    @Value("${file.upload.allowed-content-types}")
    private String allowedContentTypes;

    @Transactional
    public Result<AttachmentResponse> handle(UserContext user, Long evidenceId, MultipartFile file) {
        var evidence = evidenceAccess.requireEvidence(user, evidenceId, Permission.ATTACHMENT_CREATE);
        if (!evidence.isSuccess()) return Result.error(evidence.getErrorType(), evidence.getErrorMessage());
        Result<Void> validation = validate(file);
        if (!validation.isSuccess()) return Result.error(validation.getErrorType(), validation.getErrorMessage());
        try {
            byte[] content = file.getBytes();
            var attachment = new Attachment();
            attachment.setEvidence(evidence.getData());
            attachment.setOriginalFileName(file.getOriginalFilename());
            attachment.setContentType(file.getContentType());
            attachment.setFileSize(content.length);
            attachment.setChecksum(checksum(content));
            attachment.setStorageKey(fileStorage.store(content));
            return Result.success(mapper.toResponse(attachmentRepository.save(attachment)));
        } catch (IOException exception) {
            return Result.error("Attachment could not be stored");
        }
    }

    private Result<Void> validate(MultipartFile file) {
        if (file == null || file.isEmpty()) return Result.validation("Attachment file is required");
        if (file.getSize() > maxSizeBytes) return Result.validation("Attachment file must not exceed 10 MB");
        if (!Set.of(allowedContentTypes.split(",")).contains(file.getContentType())) return Result.validation("Attachment content type is not allowed");
        if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank() || file.getOriginalFilename().length() > 255
            || !Path.of(file.getOriginalFilename()).getFileName().toString().equals(file.getOriginalFilename())
            || file.getOriginalFilename().chars().anyMatch(Character::isISOControl)) {
            return Result.validation("Attachment file name is invalid");
        }
        return Result.success();
    }

    private String checksum(byte[] content) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
