package br.imd.ufrn.tourai.dto;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

public record UpdateItineraryRequest(List<UpdateItineraryRequestActivity> activities) {
    public static record UpdateItineraryRequestActivity(Long activityId, @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") OffsetDateTime time, Boolean completed) {

    }
}
