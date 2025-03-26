import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.iemr.common.utils.config.ConfigProperties;

@Service
public class VideoCallServiceImpl implements VideoCallService {
   	final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private String meetingLink;
    private boolean isLinkSent = false;
    private String consultationStatus = "Not Initiated";
    private String jitsiLink;

    // Constructor to initialize jitsiLink
    public VideoCallServiceImpl() {
        this.jitsiLink = ConfigProperties.getPropertyByName("video-call-url");
        logger.info("Jitsi Link fetched: " + this.jitsiLink);
    }

    @Override
    public String generateMeetingLink() {
        logger.info("generateMeetingLink: ");

        meetingLink = jitsiLink + System.currentTimeMillis();
        logger.info("Meeting link: " + meetingLink);
        return meetingLink;
    }

    @Override
    public String sendMeetingLink() throws Exception {
        if (meetingLink == null) {
            throw new Exception("Meeting link not generated yet.");
        }
        isLinkSent = true;
        return meetingLink;
    }

    @Override
    public String startCall() throws Exception {
        if (!isLinkSent) {
            throw new Exception("Cannot start without sending the link.");
        }
        consultationStatus = "Ongoing";
        return "Video consultation started";
    }

    @Override
    public String getConsultationStatus() {
        return consultationStatus;
    }
}
