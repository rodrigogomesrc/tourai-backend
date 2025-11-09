package br.imd.ufrn.tourai.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CreateItineraryRequest(Long userId, Long roadmapId, List<CreateItineraryRequestActivity> activities) {
    public record CreateItineraryRequestActivity(Long activityId, LocalDateTime time) {

    }
}
