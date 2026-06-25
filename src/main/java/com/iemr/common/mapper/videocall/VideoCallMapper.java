/*
* AMRIT – Accessible Medical Records via Integrated Technology 
* Integrated EHR (Electronic Health Records) Solution 
*
* Copyright (C) "Piramal Swasthya Management and Research Institute" 
*
* This file is part of AMRIT.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see https://www.gnu.org/licenses/.
*/

package com.iemr.common.mapper.videocall;

import java.util.List;
import org.mapstruct.Mapper;
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
