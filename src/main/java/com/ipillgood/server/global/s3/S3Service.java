package com.ipillgood.server.global.s3;

import com.ipillgood.server.global.apiPayload.exception.GeneralException;
import com.ipillgood.server.global.s3.code.S3ErrorCode;
import com.ipillgood.server.global.s3.dto.PresignedUpload;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/**
 * S3 이미지 업로드/조회 공용 인프라.
 * - 업로드: presigned PUT URL 발급 (클라가 S3에 직접 업로드)
 * - 조회: key -> public URL 변환
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private static final Map<String, String> ALLOWED_CONTENT_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp"
    );

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final S3Properties s3Properties;


    public List<PresignedUpload> createUploadUrls(ImageDirectory directory, List<String> contentTypes) {
        if (contentTypes == null || contentTypes.isEmpty()) {
            throw new GeneralException(S3ErrorCode.EMPTY_UPLOAD_REQUEST);
        }

        return contentTypes.stream()
                .map(contentType -> createUploadUrl(directory, contentType))
                .toList();
    }

    public PresignedUpload createUploadUrl(ImageDirectory directory, String contentType) {
        String extension = resolveExtension(contentType);
        String key = directory.getPrefix() + "/" + UUID.randomUUID() + "." + extension;
        String uploadUrl = presignPut(key, contentType);
        return new PresignedUpload(uploadUrl, key);
    }

    public void validateUploadedKey(ImageDirectory directory, String key) {
        if (!directory.matchesKey(key)) {
            throw new GeneralException(S3ErrorCode.INVALID_IMAGE_KEY);
        }
        if (!exists(key)) {
            throw new GeneralException(S3ErrorCode.IMAGE_NOT_FOUND);
        }
    }

    public String getPublicUrl(String key) {
        return s3Properties.publicBaseUrl() + "/" + key;
    }

    public void delete(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(s3Properties.bucket())
                    .key(key)
                    .build());
        } catch (SdkException e) {
            log.warn("S3 객체 삭제 실패: key={}", key, e);
            throw new GeneralException(S3ErrorCode.IMAGE_DELETE_FAILED);
        }
    }

    private String presignPut(String key, String contentType) {
        try {
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.bucket())
                    .key(key)
                    .contentType(contentType)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(s3Properties.presignExpiration())
                    .putObjectRequest(objectRequest)
                    .build();

            return s3Presigner.presignPutObject(presignRequest).url().toString();
        } catch (SdkException e) {
            log.warn("presigned URL 발급 실패: key={}", key, e);
            throw new GeneralException(S3ErrorCode.IMAGE_UPLOAD_URL_FAILED);
        }
    }

    private boolean exists(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(s3Properties.bucket())
                    .key(key)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            log.warn("S3 headObject 실패: key={}, status={}", key, e.statusCode(), e);
            throw new GeneralException(S3ErrorCode.IMAGE_LOOKUP_FAILED);
        }
    }

    private String resolveExtension(String contentType) {
        if (contentType == null) {
            throw new GeneralException(S3ErrorCode.INVALID_CONTENT_TYPE);
        }
        String extension = ALLOWED_CONTENT_TYPES.get(contentType.trim().toLowerCase());
        if (extension == null) {
            throw new GeneralException(S3ErrorCode.INVALID_CONTENT_TYPE);
        }
        return extension;
    }
}
