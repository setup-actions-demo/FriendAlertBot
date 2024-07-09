package edu.ivanuil.friendalertbot.service;

import edu.ivanuil.friendalertbot.dto.platform.auth.TokenDto;
import edu.ivanuil.friendalertbot.exception.EntityNotFoundException;
import edu.ivanuil.friendalertbot.exception.HttpRequestsException;
import edu.ivanuil.friendalertbot.dto.platform.CampusDto;
import edu.ivanuil.friendalertbot.dto.platform.ClusterDto;
import edu.ivanuil.friendalertbot.dto.platform.WorkplaceDto;
import edu.ivanuil.friendalertbot.dto.platform.ParticipantDto;
import edu.ivanuil.friendalertbot.dto.platform.ParticipantsListDto;
import edu.ivanuil.friendalertbot.dto.platform.CampusesDto;
import edu.ivanuil.friendalertbot.dto.platform.ClustersDto;
import edu.ivanuil.friendalertbot.dto.platform.ClusterMapDto;
import edu.ivanuil.friendalertbot.util.RequestRateUtil;
import lombok.Getter;
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
    @Getter
    private String username;
    @Value("${school21.platform.password}")
    private String password;

    private String token;
    private final RequestRateUtil requestRate = new RequestRateUtil();

    private HttpEntity<Void> getRequestEntity() {
        if (token == null || token.isEmpty())
            authorise();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", token);
        return new HttpEntity<>(headers);
    }

    public synchronized void authorise() {
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

    public double getRequestRatePerSecond() {
        return requestRate.getRatePerSecond();
    }

    public double getRequestRatePerSecondAndReset() {
        return requestRate.getRatePerSecondAndReset();
    }

    @Retryable(retryFor = HttpRequestsException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000))
    public CampusDto[] getCampuses() {
        try {
            ResponseEntity<CampusesDto> response = restTemplate.exchange(
                    GET_CAMPUSES_URL, HttpMethod.GET, getRequestEntity(), CampusesDto.class);
            requestRate.incrementRequestCount();
            return response.getBody().getCampuses();
        } catch (HttpClientErrorException.Forbidden | HttpClientErrorException.Unauthorized e) {
            authorise();
            throw new HttpRequestsException(e);
        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new HttpRequestsException(e);
        }
    }

    @Retryable(retryFor = HttpRequestsException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000))
    public ClusterDto[] getClusters(final UUID campusId) {
        try {
            ResponseEntity<ClustersDto> response = restTemplate.exchange(
                    String.format(GET_CLUSTERS_URL, campusId), HttpMethod.GET, getRequestEntity(), ClustersDto.class);
            requestRate.incrementRequestCount();
            return response.getBody().getClusters();
        } catch (HttpClientErrorException.Forbidden | HttpClientErrorException.Unauthorized e) {
            authorise();
            throw new HttpRequestsException(e);
        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new HttpRequestsException(e);
        }
    }

    @Retryable(retryFor = HttpRequestsException.class, maxAttempts = 5, backoff = @Backoff(delay = 1500))
    public WorkplaceDto[] getClusterVisitors(final Integer clusterId) {
        try {
            ResponseEntity<ClusterMapDto> response = restTemplate.exchange(
                    String.format(GET_CLUSTER_VISITORS_URL, clusterId), HttpMethod.GET,
                    getRequestEntity(), ClusterMapDto.class);
            requestRate.incrementRequestCount();
            return response.getBody().getClusterMap();
        } catch (HttpClientErrorException.Forbidden | HttpClientErrorException.Unauthorized e) {
            authorise();
            throw new HttpRequestsException(e);
        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new HttpRequestsException(e);
        }
    }

    @Retryable(retryFor = HttpRequestsException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000))
    public boolean checkIfUserExists(final String username) {
        try {
            ResponseEntity<?> response = restTemplate.exchange(
                    String.format(GET_USER_INFO_URL, username), HttpMethod.GET,
                    getRequestEntity(), ParticipantDto.class);
            requestRate.incrementRequestCount();
            return true;
        } catch (HttpClientErrorException.Forbidden | HttpClientErrorException.Unauthorized e) {
            authorise();
            throw new HttpRequestsException(e);
        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new HttpRequestsException(e);
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    @Retryable(retryFor = HttpRequestsException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000))
    public ParticipantDto getUserInfo(final String login) throws EntityNotFoundException {
        try {
            ResponseEntity<ParticipantDto> response = restTemplate.exchange(
                    String.format(GET_USER_INFO_URL, login), HttpMethod.GET,
                    getRequestEntity(), ParticipantDto.class);
            requestRate.incrementRequestCount();
            return response.getBody();
        } catch (HttpClientErrorException.Unauthorized e) {
            authorise();
            throw new HttpRequestsException(e);
        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new HttpRequestsException(e);
        } catch (HttpClientErrorException.NotFound | HttpClientErrorException.Forbidden e) {
            log.warn("Unexpected error while fetching user info for {}", login, e);
            throw new EntityNotFoundException();
        }
    }

    @Retryable(retryFor = HttpRequestsException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000))
    public ParticipantsListDto getParticipantList(final UUID campusId, final int limit, final int offset) {
        try {
            ResponseEntity<ParticipantsListDto> response = restTemplate.exchange(
                    String.format(GET_PARTICIPANT_LIST_URL, campusId, limit, offset), HttpMethod.GET,
                    getRequestEntity(), ParticipantsListDto.class);
            requestRate.incrementRequestCount();
            return response.getBody();
        } catch (HttpClientErrorException.Forbidden | HttpClientErrorException.Unauthorized e) {
            authorise();
            throw new HttpRequestsException(e);
        } catch (HttpClientErrorException.TooManyRequests e) {
            throw new HttpRequestsException(e);
        }
    }

}
