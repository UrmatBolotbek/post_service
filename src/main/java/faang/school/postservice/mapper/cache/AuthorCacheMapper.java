package faang.school.postservice.mapper.cache;

import faang.school.postservice.dto.user.UserDto;
import faang.school.postservice.model.cache.AuthorCache;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface AuthorCacheMapper {
    AuthorCache toAuthorCache(UserDto userDto);
}
