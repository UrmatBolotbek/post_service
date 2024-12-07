package faang.school.postservice.mapper.resource;

import faang.school.postservice.dto.resource.ResourceResponseDto;
import faang.school.postservice.model.Resource;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ResourceMapper {

    @Mapping(source = "post.id", target = "postId")
    ResourceResponseDto toDto(Resource resource);
}