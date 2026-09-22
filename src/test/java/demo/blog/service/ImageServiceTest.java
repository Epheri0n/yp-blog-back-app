package demo.blog.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ImageServiceTest {
    private final ImageService images = new ImageService();

    public static byte[] png() throws Exception {
        var output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", output);
        return output.toByteArray();
    }

    @Test
    void preservesImageAndDetectsTypeFromContent() throws Exception {
        byte[] content = png();
        var image = images.validate(content);
        assertEquals("image/png", image.contentType());
        assertArrayEquals(content, image.content());
    }

    @Test
    void rejectsEmptyInvalidAndOversizedContent() {
        assertThrows(BadRequestException.class, () -> images.validate(new byte[0]));
        assertThrows(BadRequestException.class, () -> images.validate(new byte[]{1, 2, 3}));
        assertThrows(BadRequestException.class, () -> images.validate(new byte[ImageService.MAX_BYTES + 1]));
    }
}
