package com.snapandpack.service;

import com.snapandpack.exception.ApiException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Saves product photos uploaded by staff. An upload is an easy way to smuggle in a harmful file, so:
 *   - only JPEG and PNG are accepted, judged by the file's first bytes, not by its name
 *   - the file must actually decode as a picture
 *   - the picture is written out again from its pixels, which drops anything hidden inside the file
 *   - the server chooses the file name; the name sent by the browser is never used
 *   - files are kept in their own folder, outside the program, and are only ever served as pictures
 */
@Service
public class UploadService {

    private static final int MAX_SIDE = 8000;

    private final Path folder;
    private final long maxBytes;

    /** Receives the upload folder and size limit from application.properties and creates the folder. */
    public UploadService(@Value("${app.upload-dir}") String folder, @Value("${app.upload-max-bytes}") long maxBytes)
            throws IOException {
        this.folder = Paths.get(folder).toAbsolutePath().normalize();
        this.maxBytes = maxBytes;
        Files.createDirectories(this.folder);
    }

    /** The folder uploaded photos are kept in. */
    public Path folder() {
        return folder;
    }

    /** Checks and saves one product photo, and returns the path to store on the product, such as uploads/ab12.jpg. */
    public String saveProductImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("Choose a photo to upload");
        }
        if (file.getSize() > maxBytes) {
            throw new ApiException(HttpStatus.PAYLOAD_TOO_LARGE, "The photo is too large. The limit is " + (maxBytes / 1024 / 1024) + " MB.");
        }
        try {
            byte[] bytes = file.getBytes();
            String format = detectFormat(bytes);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (format == null || image == null) {
                throw ApiException.badRequest("Only JPEG and PNG photos can be uploaded");
            }
            if (image.getWidth() > MAX_SIDE || image.getHeight() > MAX_SIDE) {
                throw ApiException.badRequest("The photo is too big. Each side can be at most " + MAX_SIDE + " pixels.");
            }
            String name = UUID.randomUUID() + (format.equals("png") ? ".png" : ".jpg");
            try (OutputStream out = Files.newOutputStream(folder.resolve(name))) {
                ImageIO.write(image, format, out);
            }
            return "uploads/" + name;
        } catch (IOException e) {
            throw ApiException.badRequest("That file could not be read as a photo");
        }
    }

    /** Recognises JPEG and PNG by the bytes every such file starts with. Returns "jpg", "png" or null. */
    private static String detectFormat(byte[] b) {
        if (b.length > 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (b.length > 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return "png";
        }
        return null;
    }
}
