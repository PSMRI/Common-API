package com.iemr.common.controller.customization;


import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.iemr.common.data.customization.SectionFieldsMappingDTO;
import com.iemr.common.data.customization.SectionProjectMappingDTO;
import com.iemr.common.service.customization.CustomizationService;
import com.iemr.common.utils.response.OutputResponse;

import io.swagger.v3.oas.annotations.Operation;

@RequestMapping(value = "/customization")
@RestController
public class CustomizationController {

	@Autowired
	private CustomizationService customizationService;
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@Operation(summary = "Add project")
	@RequestMapping(value = "/addProject", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String addProject(@RequestBody String request,
			@RequestHeader(value = "Authorization") String Authorization) {

		OutputResponse response = new OutputResponse();
		try {

			response.setResponse(customizationService.addProject(request, Authorization));
		} catch (Exception e) {

			response.setError(5000, e.getLocalizedMessage());
		}
		return response.toString();
	}
	@Operation(summary = "Fetch project names")
	@RequestMapping(value = "/get/projectNames/{serviceProviderId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getProjectNames(@PathVariable Integer serviceProviderId) {
		OutputResponse response = new OutputResponse();
		try {

			response.setResponse(customizationService.getProjectNames(serviceProviderId));
		} catch (Exception e) {
			logger.info("getFacility failed with error " + e.getMessage(), e);
			response.setError(e);
		}

		return response.toString();
	}

	@Operation(summary = "Update project")
	@RequestMapping(value = "/updateProject", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String updateProject(@RequestBody String request,
			@RequestHeader(value = "Authorization") String Authorization) {

		OutputResponse response = new OutputResponse();
		try {
			response.setResponse(customizationService.updateProject(request, Authorization));
		} catch (Exception e) {

			response.setError(5000, e.getLocalizedMessage());
		}
		return response.toString();
	}
	@Operation(summary = "Save project to serviceline")
	@RequestMapping(value = "/saveProjectToServiceline", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String saveProjectToServiceline(@RequestBody String request,
			@RequestHeader(value = "Authorization") String Authorization) {

		OutputResponse response = new OutputResponse();
		try {

			response.setResponse(customizationService.saveProjectToServiceline(request, Authorization));
		} catch (Exception e) {

			response.setError(5000, e.getLocalizedMessage());
		}
		return response.toString();
	}

	@Operation(summary = "Fetch project serviceline")
	@RequestMapping(value = "/fetchProjectServiceline", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String fetchProjectServiceline(@RequestBody String request,
			@RequestHeader(value = "Authorization") String Authorization) {

		OutputResponse response = new OutputResponse();
		try {

			response.setResponse(customizationService.fetchProjectServiceline(request, Authorization));
		} catch (Exception e) {

			response.setError(5000, e.getLocalizedMessage());
		}
		return response.toString();
	}
	@Operation(summary = "Update project to serviceline")
	@RequestMapping(value = "/updateProjectToServiceline", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String updateProjectToServiceline(@RequestBody String request,
			@RequestHeader(value = "Authorization") String Authorization) {

		OutputResponse response = new OutputResponse();
		try {

			response.setResponse(customizationService.updateProjectToServiceline(request, Authorization));
		} catch (Exception e) {

			response.setError(5000, e.getLocalizedMessage());
		}
		return response.toString();
	}
	@Operation(summary = "Fetch section masters")
	@RequestMapping(value = "/get/sections", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getSections() {
		OutputResponse response = new OutputResponse();
		try {

			response.setResponse(customizationService.getSections());
		} catch (Exception e) {
			logger.info("getFacility failed with error " + e.getMessage(), e);
			response.setError(e);
		}

		return response.toString();
	}
	@Operation(summary = "Update section and fields")
	@RequestMapping(value = "/updateSectionAndFields", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String updateSectionAndFields(@RequestBody String request,
			@RequestHeader(value = "Authorization") String Authorization) {

		OutputResponse response = new OutputResponse();
		try {

			response.setResponse(customizationService.updateSectionAndFields(request, Authorization));
		} catch (Exception e) {
			response.setError(5000, e.getLocalizedMessage());
		}
		return response.toString();
	}
	@Operation(summary = "Save section and fields")
	@RequestMapping(value = "/saveSectionAndFields", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String saveSectionAndFields(@RequestBody SectionFieldsMappingDTO sectionFieldsMappingDTO,
			@RequestHeader(value = "Authorization") String Authorization) {

		OutputResponse response = new OutputResponse();
		try {

			response.setResponse(customizationService.saveSectionAndFields(sectionFieldsMappingDTO, Authorization));
		} catch (Exception e) {
			response.setError(5000, e.getLocalizedMessage());
		}
		return response.toString();
	}
	@Operation(summary = "Section and project mapping")
	@RequestMapping(value = "/mapSectionToProject", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String mapSectionToProject(@RequestBody SectionProjectMappingDTO sectionProjectMappingDTO,
			@RequestHeader(value = "Authorization") String Authorization) {

		OutputResponse response = new OutputResponse();
		try {

			response.setResponse(customizationService.mapSectionToProject(sectionProjectMappingDTO, Authorization));
		} catch (Exception e) {

			response.setError(5000, e.getLocalizedMessage());
		}
		return response.toString();
	}
	@Operation(summary = "Fetch mapped sections in project")
	@RequestMapping(value = "/fetchMappedSectionsInProject", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String fetchMappedSectionsInProject(@RequestBody String request,
			@RequestHeader(value = "Authorization") String Authorization) {

		OutputResponse response = new OutputResponse();
		try {

			response.setResponse(customizationService.fetchMappedSectionsInProject(request, Authorization));
		} catch (Exception e) {

			response.setError(5000, e.getLocalizedMessage());
		}
		return response.toString();
	}
	@Operation(summary = "Fetch mapped fields")
	@RequestMapping(value = "/fetchMappedFields", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String fetchMappedFields(@RequestBody String request,
			@RequestHeader(value = "Authorization") String Authorization) {

		OutputResponse response = new OutputResponse();
		try {

			response.setResponse(customizationService.fetchMappedFields(request, Authorization));
		} catch (Exception e) {

			response.setError(5000, e.getLocalizedMessage());
		}
		return response.toString();
	}
	@Operation(summary = "Fetch all data")
	@RequestMapping(value = "/fetchAllData", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String fetchAllData(@RequestBody String request,
			@RequestHeader(value = "Authorization") String Authorization) {

		OutputResponse response = new OutputResponse();
		try {

			response.setResponse(customizationService.fetchAllData(request, Authorization));
		} catch (Exception e) {

			response.setError(5000, e.getLocalizedMessage());
		}
		return response.toString();
	}
	@Operation(summary = "Fetch field masters")
	@RequestMapping(value = "/get/fileldType", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON, headers = "Authorization")
	public String getfileldType() {
		OutputResponse response = new OutputResponse();
		try {

			response.setResponse(customizationService.getfileldType());
		} catch (Exception e) {
			logger.info("getFacility failed with error " + e.getMessage(), e);
			response.setError(e);
		}

		return response.toString();
	}
}
