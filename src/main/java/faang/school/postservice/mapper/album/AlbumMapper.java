package faang.school.postservice.mapper.album;

import faang.school.postservice.dto.album.AlbumRequestDto;
import faang.school.postservice.dto.album.AlbumRequestUpdateDto;
import faang.school.postservice.dto.album.AlbumResponseDto;
import faang.school.postservice.model.Album;
import faang.school.postservice.model.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AlbumMapper {

    Album toAlbum(AlbumRequestDto albumRequestDto);

    @Mapping(source = "postsIds", target = "posts", ignore = true)
    Album toAlbum(AlbumRequestUpdateDto albumRequestUpdateDto);

    @Mapping(source = "posts", target = "postsIds", qualifiedByName = "toIds")
    AlbumResponseDto toAlbumResponseDto(Album album);

    List<AlbumResponseDto> toAlbumResponseDtoList(List<Album> albums);

    @Named("toIds")
    default List<Long> toInternshipIds(List<Post> interns) {
        return interns == null ? null : interns.stream().map(Post::getId).toList();
    }

}
