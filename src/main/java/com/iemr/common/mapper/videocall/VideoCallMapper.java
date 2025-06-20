package com.iemr.common.mapper.videocall;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.mapstruct.IterableMapping;
import org.mapstruct.factory.Mappers;

import com.iemr.common.data.videocall.VideoCallParameters;
import com.iemr.common.model.videocall.UpdateCallRequest;
import com.iemr.common.model.videocall.UpdateCallResponse;
import com.iemr.common.model.videocall.VideoCallRequest;

@Mapper(componentModel = "spring")
public interface VideoCallMapper {
    VideoCallMapper INSTANCE = Mappers.getMapper(VideoCallMapper.class);

    VideoCallRequest videoCallToRequest(VideoCallParameters videoCall);

    VideoCallParameters videoCallToEntity(VideoCallRequest videoCallRequest);

    @IterableMapping(elementTargetType = VideoCallRequest.class)
    List<VideoCallRequest> videoCallToRequestList(List<VideoCallParameters> videoCallList);

    @IterableMapping(elementTargetType = VideoCallParameters.class)
    List<VideoCallParameters> videoCallToEntityList(List<VideoCallRequest> videoCallRequestList);

    VideoCallParameters updateRequestToVideoCall(UpdateCallRequest updateCallStatusRequest);

    UpdateCallResponse videoCallToResponse(VideoCallParameters videoCall);

    @IterableMapping(elementTargetType = VideoCallParameters.class)
    List<VideoCallParameters> updateRequestToVideoCall(List<UpdateCallRequest> updateCallStatusRequests);

    @IterableMapping(elementTargetType = UpdateCallResponse.class)
    List<UpdateCallResponse> videoCallToResponse(List<VideoCallParameters> videoCalls);
}
