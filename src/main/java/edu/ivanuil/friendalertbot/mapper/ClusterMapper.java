package edu.ivanuil.friendalertbot.mapper;

import edu.ivanuil.friendalertbot.dto.platform.ClusterDto;
import edu.ivanuil.friendalertbot.entity.ClusterEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClusterMapper {

    ClusterEntity toClusterEntity(ClusterDto clusterDto);

    List<ClusterEntity> toClusterEntityList(ClusterDto[] clusterDto);

}
