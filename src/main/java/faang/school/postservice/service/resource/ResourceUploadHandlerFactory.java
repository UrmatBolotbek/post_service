package faang.school.postservice.service.resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ResourceUploadHandlerFactory {
    private final Map<String, ResourceHandler> handlers;

    public ResourceUploadHandlerFactory(List<ResourceHandler> handlers) {
        this.handlers = handlers.stream()
                .collect(Collectors.toMap(ResourceHandler::getType, Function.identity()));
    }

    public ResourceHandler getHandler(String type) {
        ResourceHandler handler = handlers.get(type);
        log.info("handler for the type {} was assigned by ResourceHandleFactory", type);
        if (handler == null) {
            log.info("handler for the type {} was not found", type);
            throw new IllegalArgumentException("No handler found for type: " + type);
        }
        return handler;
    }
}