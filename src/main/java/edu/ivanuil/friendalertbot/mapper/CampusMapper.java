package edu.ivanuil.friendalertbot.mapper;

import edu.ivanuil.friendalertbot.dto.platform.CampusDto;
import edu.ivanuil.friendalertbot.entity.CampusEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CampusMapper {

    @Mapping(source = "shortName", target = "name")
    CampusEntity toCampusEntity(CampusDto campusDto);

    List<CampusEntity> toCampusEntityList(CampusDto[] campusDto);

}
