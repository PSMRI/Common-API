package com.iemr.common.data.cti;
import lombok.Data;

@Data  
public class DispositionCountRequest {  
    private String transaction_id = "CTI_GET_DISP_COUNT";  
    private String campaign_id;  
    private String disposition;  
    private String date;
    private String enc_flag; 
}