package com.iemr.common.service.videocall;

import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iemr.common.data.videocall.VideoCallParameters;
import com.iemr.common.mapper.videocall.VideoCallMapper;
import com.iemr.common.model.videocall.UpdateCallRequest;
import com.iemr.common.model.videocall.VideoCallRequest;
import com.iemr.common.repository.videocall.VideoCallParameterRepository;
import com.iemr.common.utils.config.ConfigProperties;
import com.iemr.common.utils.mapper.OutputMapper;

@Service
public class VideoCallServiceImpl implements VideoCallService {
	final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@Autowired
    private VideoCallParameterRepository videoCallRepository;
    
	@Autowired
    private VideoCallMapper videoCallMapper;

    private String meetingLink;
    private boolean isLinkSent = false;
    private String consultationStatus = "Not Initiated";
    private String jitsiLink;

    public VideoCallServiceImpl() {
        this.jitsiLink = ConfigProperties.getPropertyByName("video-call-url");
        logger.info("Jitsi Link fetched: " + this.jitsiLink);
    }

    @Override
    public String generateMeetingLink() {
        meetingLink=jitsiLink+RandomStringUtils.randomAlphanumeric(8);
        logger.info("Meeting link: " + meetingLink);
        return meetingLink;
    }

    @Override
    public String sendMeetingLink(VideoCallRequest request) throws Exception {
        if (meetingLink == null) {
            throw new Exception("Meeting link not generated yet.");
        }

        isLinkSent = true;
        VideoCallParameters videoCallEntity = videoCallMapper.videoCallToEntity(request);
        videoCallEntity.setMeetingLink(meetingLink);

        videoCallRepository.save(videoCallEntity);

        return OutputMapper.gsonWithoutExposeRestriction().toJson(videoCallMapper.videoCallToRequest(videoCallEntity));
    }

    @Override
    public String updateCallStatus(UpdateCallRequest callRequest) throws Exception {
        VideoCallParameters videoCall = null;

        VideoCallParameters requestEntity = videoCallMapper.updateRequestToVideoCall(callRequest);

        int updateCount = videoCallRepository.updateCallStatusByMeetingLink(
            requestEntity.getMeetingLink(),
            requestEntity.getCallStatus(),
            requestEntity.getCallDuration(),
            requestEntity.getModifiedBy()
        );

        if (updateCount > 0) {
            videoCall = videoCallRepository.findByMeetingLink(requestEntity.getMeetingLink());
        } else {
            throw new Exception("Failed to update the call status");
        }

        return OutputMapper.gsonWithoutExposeRestriction()
            .toJson(videoCallMapper.videoCallToResponse(videoCall));
    }



}
