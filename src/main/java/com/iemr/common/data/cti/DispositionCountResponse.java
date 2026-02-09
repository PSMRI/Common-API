package com.iemr.common.data.cti;
import lombok.Data;
import com.google.gson.annotations.Expose;
import java.util.List;

@Data  
public class DispositionCountResponse {  
    @Expose  
    private CTIResponse response;  
      
    @Expose  
    private Integer count;  
      
    @Expose  
    private String campaign_id;  
      
    @Expose  
    private String disposition;  
      
    @Expose  
    private String date;  
      
    @Expose  
    private List<DispositionData> dispositionData;  
      
    @Expose  
    private String encryptedData; // for enc_flag = "1"  
      
    // Inner class for detailed disposition data  
    @Data  
    public static class DispositionData {  
        @Expose  
        private String disposition;  
          
        @Expose  
        private Integer count;  
          
        @Expose  
        private String date;  
    }  
}