package br.imd.ufrn.tourai.dto;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public record CreateItineraryRequest(Long roadmapId, List<CreateItineraryRequestActivity> activities) {
    public static record CreateItineraryRequestActivity(Long activityId, @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") OffsetDateTime time) {

    }
}
