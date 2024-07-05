package edu.ivanuil.friendalertbot.service;

import edu.ivanuil.friendalertbot.dto.platform.auth.TokenDto;
import edu.ivanuil.friendalertbot.exception.EntityNotFoundException;
import edu.ivanuil.friendalertbot.exception.TooManyRequestsException;
import edu.ivanuil.friendalertbot.dto.platform.CampusDto;
import edu.ivanuil.friendalertbot.dto.platform.ClusterDto;
import edu.ivanuil.friendalertbot.dto.platform.WorkplaceDto;
import edu.ivanuil.friendalertbot.dto.platform.ParticipantDto;
import edu.ivanuil.friendalertbot.dto.platform.ParticipantsListDto;
import edu.ivanuil.friendalertbot.dto.platform.CampusesDto;
import edu.ivanuil.friendalertbot.dto.platform.ClustersDto;
import edu.ivanuil.friendalertbot.dto.platform.ClusterMapDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
@Slf4j
public class School21PlatformBinding {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String GET_TOKEN_URL =
            "https://auth.sberclass.ru/auth/realms/EduPowerKeycloak/protocol/openid-connect/token";
    private static final String GET_CAMPUSES_URL =
            "https://edu-api.21-school.ru/services/21-school/api/v1/campuses";
    private static final String GET_CLUSTERS_URL =
            "https://edu-api.21-school.ru/services/21-school/api/v1/campuses/%s/clusters";
    private static final String GET_CLUSTER_VISITORS_URL =
            "https://edu-api.21-school.ru/services/21-school/api/v1/clusters/%s/map?limit=1000&offset=0&occupied=true";
    private static final String GET_USER_INFO_URL =
            "https://edu-api.21-school.ru/services/21-school/api/v1/participants/%s";
    private static final String GET_PARTICIPANT_LIST_URL =
            "https://edu-api.21-school.ru/services/21-school/api/v1/campuses/%s/participants?limit=%d&offset=%d";

    @Value("${school21.platform.username}")
    private String username;
    @Value("${school21.platform.password}")
    private String password;

    private String token;

    private HttpEntity<Void> getRequestEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", token);
        return new HttpEntity<>(headers);
    }

    public void authorise() {
        log.info("School21 platform token not found or obsolete, attempting to authorise");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("client_id", "s21-open-api");
        map.add("username", username);
        map.add("password", password);
        var requestEntity = new HttpEntity<>(map, headers);

        ResponseEntity<TokenDto> response = restTemplate.exchange(
                GET_TOKEN_URL, HttpMethod.POST, requestEntity, TokenDto.class);

        token = response.getBody().getTokenType() + " " + response.getBody().getAccessToken();
        log.info("School21 platform authorised");
    }

    @Retryable(retryFor = TooManyRequestsException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000))
    public CampusDto[] getCampuses() {
        if (token == null || token.isEmpty())
            authorise();

        try {
            ResponseEntity<CampusesDto> response = restTemplate.exchange(
                    GET_CAMPUSES_URL, HttpMethod.GET, getRequestEntity(), CampusesDto.class);
            return response.getBody().getCampuses();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("401"))
                authorise();
            throw new TooManyRequestsException(e);
        }
    }

    @Retryable(retryFor = TooManyRequestsException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000))
    public ClusterDto[] getClusters(final UUID campusId) {
        if (token == null || token.isEmpty())
            authorise();

        try {
            ResponseEntity<ClustersDto> response = restTemplate.exchange(
                    String.format(GET_CLUSTERS_URL, campusId), HttpMethod.GET, getRequestEntity(), ClustersDto.class);
            return response.getBody().getClusters();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("401"))
                authorise();
            throw new TooManyRequestsException(e);
        }
    }

    @Retryable(retryFor = TooManyRequestsException.class, maxAttempts = 5, backoff = @Backoff(delay = 1500))
    public WorkplaceDto[] getClusterVisitors(final Integer clusterId) {
        if (token == null || token.isEmpty())
            authorise();

        try {
            ResponseEntity<ClusterMapDto> response = restTemplate.exchange(
                    String.format(GET_CLUSTER_VISITORS_URL, clusterId), HttpMethod.GET,
                    getRequestEntity(), ClusterMapDto.class);
            return response.getBody().getClusterMap();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("401"))
                authorise();
            throw new TooManyRequestsException(e);
        }
    }

    @Retryable(retryFor = TooManyRequestsException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000))
    public boolean checkIfUserExists(final String username) {
        if (token == null || token.isEmpty())
            authorise();

        try {
            ResponseEntity<?> response = restTemplate.exchange(
                    String.format(GET_USER_INFO_URL, username), HttpMethod.GET,
                    getRequestEntity(), ParticipantDto.class);
            return true;
        } catch (HttpClientErrorException e) {
            if (e.getMessage().contains("401"))
                authorise();
            if (e.getMessage().contains("404"))
                return false;
            else
                throw new TooManyRequestsException(e);
        }
    }

    @Retryable(retryFor = TooManyRequestsException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000))
    public ParticipantDto getUserInfo(final String login) throws EntityNotFoundException {
        if (token == null || token.isEmpty())
            authorise();

        try {
            ResponseEntity<ParticipantDto> response = restTemplate.exchange(
                    String.format(GET_USER_INFO_URL, login), HttpMethod.GET,
                    getRequestEntity(), ParticipantDto.class);
            return response.getBody();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("401") || e.getMessage().contains("403"))
                authorise();
            if (e.getMessage().contains("404"))
                throw new EntityNotFoundException();
            throw new TooManyRequestsException(e);
        }
    }

    @Retryable(retryFor = TooManyRequestsException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000))
    public ParticipantsListDto getParticipantList(final UUID campusId, final int limit, final int offset) {
        if (token == null || token.isEmpty())
            authorise();

        try {
            ResponseEntity<ParticipantsListDto> response = restTemplate.exchange(
                    String.format(GET_PARTICIPANT_LIST_URL, campusId, limit, offset), HttpMethod.GET,
                    getRequestEntity(), ParticipantsListDto.class);
            return response.getBody();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("401"))
                authorise();
            throw new TooManyRequestsException(e);
        }
    }

}
