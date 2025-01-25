package faang.school.postservice.mapper.cache;

import faang.school.postservice.dto.post.PostResponseDto;
import faang.school.postservice.model.cache.PostCache;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface PostCacheMapper {
    PostCache toPostCache(PostResponseDto postDto);

    PostResponseDto toDto(PostCache postCache);
}
