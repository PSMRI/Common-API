package com.iemr.common.data.cti;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.Expose;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DispositionCountResponse {
    @Expose
    private String status;

    @Expose
    private Integer code;

    @Expose
    private String failure_reason;

    @Expose
    private Object data;

    // Inner class for campaign data
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CampaignData {
        @Expose
        private String campaign_name;

        @Expose
        private List<DispositionDetail> dispositions;
    }

    // Inner class for disposition detail
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DispositionDetail {
        @Expose
        private String call_status_disposition;

        @Expose
        private String disposition_count;
    }
}
