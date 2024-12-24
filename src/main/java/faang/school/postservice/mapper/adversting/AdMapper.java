package faang.school.postservice.mapper.adversting;

import faang.school.postservice.dto.adversting.AdDto;
import faang.school.postservice.model.Post;
import faang.school.postservice.model.ad.Ad;
import faang.school.postservice.model.enums.AdverstisingPeriod;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdMapper {
    @Mapping(source = "ad.post.id", target = "postId")
    @Mapping(source = "ad.buyerId", target = "userId")
    @Mapping(source = "period.days", target = "duration")
    @Mapping(source = "period.price", target = "price")
    @Mapping(source = "ad.startDate", target = "startDate")
    @Mapping(source = "ad.endDate", target = "endDate")
    AdDto toDto(Ad ad, AdverstisingPeriod period);

    @Mapping(source = "post", target = "post")
    @Mapping(source = "userId", target = "buyerId")
    @Mapping(source = "now", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    Ad toEntity(Post post, long userId, LocalDateTime now, LocalDateTime endDate);
}
