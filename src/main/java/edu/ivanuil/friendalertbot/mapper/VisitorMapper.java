package edu.ivanuil.friendalertbot.mapper;

import edu.ivanuil.friendalertbot.dto.platform.WorkplaceDto;
import edu.ivanuil.friendalertbot.entity.VisitorEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VisitorMapper {

    VisitorEntity toVisitorEntity(WorkplaceDto workplaceDto);

    List<VisitorEntity> toVisitorList(WorkplaceDto[] workplaceDto);

}
