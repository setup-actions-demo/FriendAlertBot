package edu.ivanuil.friendalertbot.mapper;

import edu.ivanuil.friendalertbot.dto.platform.CampusDto;
import edu.ivanuil.friendalertbot.dto.platform.ParticipantDto;
import edu.ivanuil.friendalertbot.entity.ParticipantEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ParticipantMapper {

    @Mapping(source = "className", target = "className")
    ParticipantEntity toParticipantEntity(ParticipantDto participantDto);

    default String map(CampusDto campus) {
        return campus.getShortName();
    }

}
