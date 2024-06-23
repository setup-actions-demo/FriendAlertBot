package edu.ivanuil.friendalertbot.service;

import edu.ivanuil.friendalertbot.exception.TooManyRequestsException;
import edu.ivanuil.friendalertbot.dto.platform.*;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class School21PlatformBinding {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String GET_CAMPUSES_URL = "https://edu-api.21-school.ru/services/21-school/api/v1/campuses";
    private static final String GET_CLUSTERS_URL = "https://edu-api.21-school.ru/services/21-school/api/v1/campuses/%s/clusters";
    private static final String GET_CLUSTER_VISITORS_URL = "https://edu-api.21-school.ru/services/21-school/api/v1/clusters/%s/map?limit=1000&offset=0&occupied=true";
    private static final String GET_USER_INFO_URL = "https://edu-api.21-school.ru/services/21-school/api/v1/participants/%s";

    private static final String TOKEN = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJ5V29landCTmxROWtQVEpFZnFpVzRrc181Mk1KTWkwUHl2RHNKNlgzdlFZIn0.eyJleHAiOjE3MTkxNjc2ODYsImlhdCI6MTcxOTEzMTY4NywiYXV0aF90aW1lIjoxNzE5MTMxNjg2LCJqdGkiOiJhZDZmZDM1Zi1iMDcxLTRmZGEtYmQ1MC03NGZjZTYxY2QxNDIiLCJpc3MiOiJodHRwczovL2F1dGguc2JlcmNsYXNzLnJ1L2F1dGgvcmVhbG1zL0VkdVBvd2VyS2V5Y2xvYWsiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiNTgzZDJmZTYtYjIzYi00MzQyLWFhYWMtZDAyZjY4MTY3N2RlIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoic2Nob29sMjEiLCJub25jZSI6IjU3YzJjMWEwLThiY2QtNDhiOC04NDE5LTUzNDNiZjFlNjc5NiIsInNlc3Npb25fc3RhdGUiOiJlODUzNjA4MS02Y2FlLTRiZjctODQ4Mi0yMTlkMTQ4MjEyMDgiLCJhY3IiOiIxIiwiYWxsb3dlZC1vcmlnaW5zIjpbImh0dHBzOi8vZWR1LjIxLXNjaG9vbC5ydSIsImh0dHBzOi8vZWR1LWFkbWluLjIxLXNjaG9vbC5ydSJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiZGVmYXVsdC1yb2xlcy1lZHVwb3dlcmtleWNsb2FrIiwib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoib3BlbmlkIHByb2ZpbGUgZW1haWwiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwidXNlcl9pZCI6IjkxYzM1NWNhLWMwNjctNDBjMC1hNWFmLWUwOWRjOTYwMWQ5NCIsIm5hbWUiOiJGb25kYSBBdGFoYmFoIiwiYXV0aF90eXBlX2NvZGUiOiJkZWZhdWx0IiwicHJlZmVycmVkX3VzZXJuYW1lIjoiZm9uZGFhdGFAc3R1ZGVudC4yMS1zY2hvb2wucnUiLCJnaXZlbl9uYW1lIjoiRm9uZGEiLCJmYW1pbHlfbmFtZSI6IkF0YWhiYWgiLCJlbWFpbCI6ImZvbmRhYXRhQHN0dWRlbnQuMjEtc2Nob29sLnJ1In0.BD8pFqK_gAft-y6Pbo1UsFDs1_91C4KWAUua2UxSir85uT6P-IoeSJ2dPZ2xIvhTjQu79ggMk3BpobKved5vhf-pnSyz_vHcQDJYslxXzwEoaPu-i6LaPqBu7HEvE5IZ5MvtenwlW5DoKxxg7wiUVeNniLEYFAYptPqZN7ZvVMNPvRrk7MgVcGy0qrHExfXtBD9vcSSXdir-EMTbbHiyIj2D3QatGupUI8vKiS3DU1w-DRYtIIZCXY-1Xx_A7qkw8uF_AxIJ9y6R_eaT0ij12nwC74SzvpqEgqd0BVzRrVtF5PkwRoopAUmYQQLgaaoboDo7LLQnJseAhYzLgxWhjg";

    private static HttpEntity<Void> getRequestEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", TOKEN);
        return new HttpEntity<>(headers);
    }

    @Retryable(retryFor = TooManyRequestsException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000))
    public CampusDto[] getCampuses() {
        try {
            ResponseEntity<CampusesDto> response = restTemplate.exchange(
                    GET_CAMPUSES_URL, HttpMethod.GET, getRequestEntity(), CampusesDto.class);
            return response.getBody().getCampuses();
        } catch (RuntimeException e) {
            throw new TooManyRequestsException(e);
        }
    }

    @Retryable(retryFor = TooManyRequestsException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000))
    public ClusterDto[] getClusters(UUID campusId) {
        try {
            ResponseEntity<ClustersDto> response = restTemplate.exchange(
                    String.format(GET_CLUSTERS_URL, campusId), HttpMethod.GET, getRequestEntity(), ClustersDto.class);
            return response.getBody().getClusters();
        } catch (RuntimeException e) {
            throw new TooManyRequestsException(e);
        }
    }

    @Retryable(retryFor = TooManyRequestsException.class, maxAttempts = 5, backoff = @Backoff(delay = 1500))
    public WorkplaceDto[] getClusterVisitors(Integer clusterId) {
        try {
            ResponseEntity<ClusterMapDto> response = restTemplate.exchange(
                    String.format(GET_CLUSTER_VISITORS_URL, clusterId), HttpMethod.GET,
                    getRequestEntity(), ClusterMapDto.class);
            return response.getBody().getClusterMap();
        } catch (RuntimeException e) {
            throw new TooManyRequestsException(e);
        }
    }

    @Retryable(retryFor = TooManyRequestsException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000))
    public boolean checkIfUserExists(String username) {
        try {
            ResponseEntity<?> response = restTemplate.exchange(
                    String.format(GET_USER_INFO_URL, username), HttpMethod.GET,
                    getRequestEntity(), ParticipantDto.class);
            return true;
        } catch (HttpClientErrorException e) {
            if (e.getMessage().contains("404"))
                return false;
            else
                throw new TooManyRequestsException(e);
        }
    }

}
