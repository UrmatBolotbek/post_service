package faang.school.postservice.validator.resource;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ResourceFileSizeValidator implements ConstraintValidator<ValidResourceFileSize, Object> {

    private long maxSizeInBytes;
    private String resourceType;

    @Override
    public void initialize(ValidResourceFileSize constraintAnnotation) {
        this.maxSizeInBytes = constraintAnnotation.maxSizeInBytes();
        this.resourceType = constraintAnnotation.resourceType();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }


        if (value instanceof MultipartFile) {
            return isValidFile((MultipartFile) value, context);
        }


        if (value instanceof List<?> files) {
            return files.stream()
                    .filter(MultipartFile.class::isInstance)
                    .map(MultipartFile.class::cast)
                    .allMatch(file -> isValidFile(file, context));
        }

        return false;
    }

    private boolean isValidFile(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null) {
            return true;
        }

        if (file.getSize() > maxSizeInBytes) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("File '%s' exceeds the maximum allowed size for %s: %d bytes.",
                            file.getOriginalFilename(), resourceType, maxSizeInBytes)
            ).addConstraintViolation();
            return false;
        }
        return true;
    }
}