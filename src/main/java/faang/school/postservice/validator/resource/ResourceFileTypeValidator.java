package faang.school.postservice.validator.resource;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ResourceFileTypeValidator implements ConstraintValidator<ValidResourceFileType, Object> {

    private String resourceType;

    @Override
    public void initialize(ValidResourceFileType constraintAnnotation) {
        this.resourceType = constraintAnnotation.resourceType();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        if (value instanceof MultipartFile) {
            return isValidFileType((MultipartFile) value);
        }

        if (value instanceof List<?> files) {
            return files.stream()
                    .filter(MultipartFile.class::isInstance)
                    .map(MultipartFile.class::cast)
                    .allMatch(this::isValidFileType);
        }

        return false;
    }

    private boolean isValidFileType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return true;
        }

        String contentType = file.getContentType();
        if (resourceType.equals("image")) {
            return contentType != null && contentType.startsWith("image/");
        } else if (resourceType.equals("audio")) {
            return contentType != null && contentType.startsWith("audio/");
        }

        return false;
    }
}