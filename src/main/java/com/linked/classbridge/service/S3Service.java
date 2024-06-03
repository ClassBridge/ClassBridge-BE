package com.linked.classbridge.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.linked.classbridge.exception.RestApiException;
import com.linked.classbridge.type.ErrorCode;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class S3Service {

    private final AmazonS3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private static final String[] IMAGE_EXTENSIONS =
            {"jpg", "jpeg", "png", "gif", "bmp", "svg", "webp"};

    private static final String REVIEW_FOLDER = "review/";

    private static final String USER_PROFILE_FOLDER = "userProfile/";

    private static final String ONE_DAY_CLASS_FOLDER = "oneDayClass/";

    @Transactional
    public String uploadReviewImage(MultipartFile image) {
        return uploadImage(image, REVIEW_FOLDER);
    }

    @Transactional
    public String uploadOneDayClassImage(MultipartFile image) {
        return uploadImage(image, ONE_DAY_CLASS_FOLDER);
    }

    /**
     * S3에 파일 업로드
     *
     * @param file   파일
     * @param folder 폴더
     * @return 업로드된 파일 URL
     */
    @Transactional
    public String uploadImage(MultipartFile file, String folder) {
        String fileName = folder + UUID.randomUUID() + file.getOriginalFilename();

        validateImageFile(file);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try {
            s3Client.putObject(
                    new PutObjectRequest(bucket, fileName, file.getInputStream(), metadata)
                            .withCannedAcl(CannedAccessControlList.PublicRead)
            );
            return s3Client.getUrl(bucket, fileName).toString();
        } catch (Exception e) {
            log.error("Failed to upload image", e);
            throw new RestApiException(ErrorCode.FAILED_TO_UPLOAD_IMAGE);
        }
    }

    /**
     * S3에 저장된 이미지 삭제
     *
     * @param url 이미지 URL
     */
    @Transactional
    public void delete(String url) {
        try {
            s3Client.deleteObject(bucket, getFileNameFromURL(url));
        } catch (Exception e) {
            log.error("Failed to delete image", e);
            throw new RestApiException(ErrorCode.FAILED_TO_DELETE_IMAGE);
        }
    }

    public String getFileNameFromURL(String url) {
        return url.substring(url.indexOf("com/") + 4);
    }

    private void validateImageFile(MultipartFile file) {
        String extension = Objects.requireNonNull(file.getOriginalFilename())
                .substring(file.getOriginalFilename().lastIndexOf(".") + 1)
                .toLowerCase();
        for (String imageExtension : IMAGE_EXTENSIONS) {
            if (extension.equals(imageExtension)) {
                return;
            }
        }
        throw new RestApiException(ErrorCode.INVALID_IMAGE_FILE_EXTENSION);
    }
}
