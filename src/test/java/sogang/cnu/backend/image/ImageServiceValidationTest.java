package sogang.cnu.backend.image;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import sogang.cnu.backend.common.exception.BadRequestException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 업로드 파일 검증(validateImage)만 대상으로 하는 단위 테스트.
 * validateImage는 저장소/설정 값을 사용하지 않으므로 의존성 주입 없이 생성한다.
 */
class ImageServiceValidationTest {

    private final ImageService imageService = new ImageService(null);

    // 유효 시그니처를 가진 최소 헤더(뒤쪽 바이트는 의미 없음)
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
    private static final byte[] WEBP = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P', 0, 0, 0, 0};
    private static final byte[] GIF = {'G', 'I', 'F', '8', '9', 'a', 0, 0, 0, 0, 0, 0};
    private static final byte[] HTML = "<html><script>alert(1)</script></html>".getBytes();

    private MockMultipartFile file(String name, String contentType, byte[] content) {
        return new MockMultipartFile("file", name, contentType, content);
    }

    @Test
    void acceptsValidJpg() throws Exception {
        assertThat(imageService.validateImage(file("photo.jpg", "image/jpeg", JPEG))).isEqualTo("jpg");
    }

    @Test
    void acceptsValidJpeg() throws Exception {
        assertThat(imageService.validateImage(file("photo.JPEG", "image/jpeg", JPEG))).isEqualTo("jpeg");
    }

    @Test
    void acceptsValidPng() throws Exception {
        assertThat(imageService.validateImage(file("photo.png", "image/png", PNG))).isEqualTo("png");
    }

    @Test
    void acceptsValidWebp() throws Exception {
        assertThat(imageService.validateImage(file("photo.webp", "image/webp", WEBP))).isEqualTo("webp");
    }

    @Test
    void rejectsEmptyFile() {
        assertThatThrownBy(() -> imageService.validateImage(file("photo.jpg", "image/jpeg", new byte[0])))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsOversize() {
        byte[] big = new byte[10 * 1024 * 1024 + 1];
        big[0] = (byte) 0xFF; big[1] = (byte) 0xD8; big[2] = (byte) 0xFF;
        assertThatThrownBy(() -> imageService.validateImage(file("photo.jpg", "image/jpeg", big)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsSvg() {
        byte[] svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"><script/></svg>".getBytes();
        assertThatThrownBy(() -> imageService.validateImage(file("evil.svg", "image/svg+xml", svg)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void acceptsValidGif() throws Exception {
        assertThat(imageService.validateImage(file("anim.gif", "image/gif", GIF))).isEqualTo("gif");
    }

    @Test
    void rejectsGifExtensionWithNonGifContent() {
        // 확장자 gif + Content-Type image/gif 이지만 실제 내용이 HTML → 매직바이트 불일치 → 거부
        assertThatThrownBy(() -> imageService.validateImage(file("evil.gif", "image/gif", HTML)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsHtml() {
        assertThatThrownBy(() -> imageService.validateImage(file("evil.html", "text/html", HTML)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsMissingExtension() {
        assertThatThrownBy(() -> imageService.validateImage(file("photo", "image/jpeg", JPEG)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsDoubleExtensionEndingHtml() {
        // test.png.html → 마지막 확장자 html → 거부
        assertThatThrownBy(() -> imageService.validateImage(file("test.png.html", "image/png", PNG)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsHtmlDisguisedAsPngByContentTypeAndExtension() {
        // 확장자 png + Content-Type image/png 이지만 실제 내용은 HTML → 매직바이트 불일치 → 거부
        assertThatThrownBy(() -> imageService.validateImage(file("evil.png", "image/png", HTML)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsHtmlDisguisedWithImagePngContentType() {
        // evil.html + Content-Type image/png → 확장자 allowlist에서 거부
        assertThatThrownBy(() -> imageService.validateImage(file("evil.html", "image/png", PNG)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsExtensionSignatureMismatch() {
        // 확장자 jpg + Content-Type image/jpeg 이지만 실제 PNG → 거부
        assertThatThrownBy(() -> imageService.validateImage(file("image.jpg", "image/jpeg", PNG)))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsContentTypeSignatureMismatch() {
        // 확장자 png + 실제 PNG 이지만 Content-Type image/jpeg → 거부
        assertThatThrownBy(() -> imageService.validateImage(file("image.png", "image/jpeg", PNG)))
                .isInstanceOf(BadRequestException.class);
    }
}
