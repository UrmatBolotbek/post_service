package faang.school.postservice.service.resource;

import jakarta.annotation.Nullable;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MultipartFileAdapter implements MultipartFile {
    private final byte[] fileContent;
    private final String originalFilename;
    private final String contentType;

    public MultipartFileAdapter(byte[] fileContent, String originalFilename, String contentType) {
        this.fileContent = fileContent;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
    }

    @Override
    @NonNull
    public String getName() {
        return originalFilename;
    }

    @Override
    @NonNull
    public String getOriginalFilename() {
        return originalFilename;
    }

    @Override
    @Nullable
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return fileContent.length == 0;
    }

    @Override
    public long getSize() {
        return fileContent.length;
    }

    @Override
    @NonNull
    public byte[] getBytes() throws IOException {
        return fileContent;
    }

    @Override
    @NonNull
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(fileContent);
    }

    @Override
    public void transferTo(@NonNull java.io.File dest) throws IOException, IllegalStateException {
        throw new UnsupportedOperationException("Not implemented");
    }
}
