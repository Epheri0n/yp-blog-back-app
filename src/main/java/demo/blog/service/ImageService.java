package demo.blog.service;

import demo.blog.model.PostImage;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

@Service
public class ImageService {
    public static final int MAX_BYTES = 5 * 1024 * 1024;
    private static final long MAX_PIXELS = 20_000_000;
    private static final Map<String, String> CONTENT_TYPES = Map.of(
            "png", "image/png", "jpeg", "image/jpeg", "gif", "image/gif");

    public PostImage validate(byte[] bytes) {
        if (bytes.length == 0 || bytes.length > MAX_BYTES) {
            throw new BadRequestException("Image must contain between 1 byte and 5 MiB");
        }
        try (var stream = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
            var readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) {
                throw new BadRequestException("Expected a PNG, JPEG or GIF image");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(stream);
                String type = CONTENT_TYPES.get(reader.getFormatName().toLowerCase(Locale.ROOT));
                if (type == null || (long) reader.getWidth(0) * reader.getHeight(0) > MAX_PIXELS) {
                    throw new BadRequestException("Unsupported image format or image larger than 20 megapixels");
                }
                reader.read(0);
                return new PostImage(bytes, type);
            } finally {
                reader.dispose();
            }
        } catch (IOException exception) {
            throw new BadRequestException("Invalid image");
        }
    }
}
