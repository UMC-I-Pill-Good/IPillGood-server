package com.ipillgood.server.global.s3;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class S3ServiceTest {

    private static final String PUBLIC_BASE_URL = "https://ipillgood-bucket.s3.ap-northeast-2.amazonaws.com";

    @Test
    void getPublicUrlReturnsPublicUrlWithKey() {
        S3Service s3Service = s3Service(PUBLIC_BASE_URL);

        String publicUrl = s3Service.getPublicUrl("ingredients/2.png");

        assertThat(publicUrl).isEqualTo(PUBLIC_BASE_URL + "/ingredients/2.png");
    }

    @Test
    void getPublicUrlRemovesTrailingSlashFromBaseUrl() {
        S3Service s3Service = s3Service(PUBLIC_BASE_URL + "/");

        String publicUrl = s3Service.getPublicUrl("ingredients/2.png");

        assertThat(publicUrl).isEqualTo(PUBLIC_BASE_URL + "/ingredients/2.png");
    }

    @Test
    void getPublicUrlRemovesLeadingSlashFromKey() {
        S3Service s3Service = s3Service(PUBLIC_BASE_URL);

        String publicUrl = s3Service.getPublicUrl("/ingredients/2.png");

        assertThat(publicUrl).isEqualTo(PUBLIC_BASE_URL + "/ingredients/2.png");
    }

    @Test
    void getPublicUrlReturnsNullWhenKeyIsNull() {
        S3Service s3Service = s3Service(PUBLIC_BASE_URL);

        String publicUrl = s3Service.getPublicUrl(null);

        assertThat(publicUrl).isNull();
    }

    @Test
    void getPublicUrlReturnsNullWhenKeyIsBlank() {
        S3Service s3Service = s3Service(PUBLIC_BASE_URL);

        String publicUrl = s3Service.getPublicUrl(" ");

        assertThat(publicUrl).isNull();
    }

    private S3Service s3Service(String publicBaseUrl) {
        S3Properties s3Properties = new S3Properties(
                "ipillgood-bucket",
                "ap-northeast-2",
                "access-key",
                "secret-key",
                publicBaseUrl,
                Duration.ofMinutes(5)
        );
        return new S3Service(null, null, s3Properties);
    }
}
