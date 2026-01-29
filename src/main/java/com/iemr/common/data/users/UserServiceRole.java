package com.iemr.common.data.users;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "v_userservicerolemapping", schema = "db_iemr")
public class UserServiceRole {
    private Integer userId;
    private int usrMappingId;
    private String name;
    private String userName;
    private Short serviceId;
    private String serviceName;
    private Boolean isNational;
    private Integer stateId;
    private String stateName;
    private Integer workingDistrictId;
    private String workingDistrictName;
    private Integer workingLocationId;
    private Short serviceProviderId;
    private String locationName;
    private String workingLocationAddress;
    private Integer roleId;
    private String roleName;
    private Integer providerServiceMapId;
    private String agentId;
    private Short psmStatusId;
    private String psmStatus;
    private Boolean userServciceRoleDeleted;
    private Boolean userDeleted;
    private Boolean serviceProviderDeleted;
    private Boolean roleDeleted;
    private Boolean providerServiceMappingDeleted;
    private Boolean isInbound;
    private Boolean isOutbound;
    private Integer blockid;
    private String blockname;
    private String villageid;
    private String villagename;

    @Basic
    @Column(name = "UserID")
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Basic
    @Column(name = "USRMappingID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getUsrMappingId() {
        return usrMappingId;
    }

    public void setUsrMappingId(int usrMappingId) {
        this.usrMappingId = usrMappingId;
    }

    @Basic
    @Column(name = "Name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "UserName")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Basic
    @Column(name = "ServiceID")
    public Short getServiceId() {
        return serviceId;
    }

    public void setServiceId(Short serviceId) {
        this.serviceId = serviceId;
    }

    @Basic
    @Column(name = "ServiceName")
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Basic
    @Column(name = "IsNational")
    public Boolean getNational() {
        return isNational;
    }

    public void setNational(Boolean national) {
        isNational = national;
    }

    @Basic
    @Column(name = "StateID")
    public Integer getStateId() {
        return stateId;
    }

    public void setStateId(Integer stateId) {
        this.stateId = stateId;
    }

    @Basic
    @Column(name = "StateName")
    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    @Basic
    @Column(name = "WorkingDistrictID")
    public Integer getWorkingDistrictId() {
        return workingDistrictId;
    }

    public void setWorkingDistrictId(Integer workingDistrictId) {
        this.workingDistrictId = workingDistrictId;
    }

    @Basic
    @Column(name = "WorkingDistrictName")
    public String getWorkingDistrictName() {
        return workingDistrictName;
    }

    public void setWorkingDistrictName(String workingDistrictName) {
        this.workingDistrictName = workingDistrictName;
    }

    @Basic
    @Column(name = "WorkingLocationID")
    public Integer getWorkingLocationId() {
        return workingLocationId;
    }

    public void setWorkingLocationId(Integer workingLocationId) {
        this.workingLocationId = workingLocationId;
    }

    @Basic
    @Column(name = "ServiceProviderID")
    public Short getServiceProviderId() {
        return serviceProviderId;
    }

    public void setServiceProviderId(Short serviceProviderId) {
        this.serviceProviderId = serviceProviderId;
    }

    @Basic
    @Column(name = "LocationName")
    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    @Basic
    @Column(name = "WorkingLocationAddress")
    public String getWorkingLocationAddress() {
        return workingLocationAddress;
    }

    public void setWorkingLocationAddress(String workingLocationAddress) {
        this.workingLocationAddress = workingLocationAddress;
    }

    @Basic
    @Column(name = "RoleID")
    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    @Basic
    @Column(name = "RoleName")
    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Basic
    @Column(name = "ProviderServiceMapID")
    public Integer getProviderServiceMapId() {
        return providerServiceMapId;
    }

    public void setProviderServiceMapId(Integer providerServiceMapId) {
        this.providerServiceMapId = providerServiceMapId;
    }

    @Basic
    @Column(name = "AgentID")
    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    @Basic
    @Column(name = "PSMStatusID")
    public Short getPsmStatusId() {
        return psmStatusId;
    }

    public void setPsmStatusId(Short psmStatusId) {
        this.psmStatusId = psmStatusId;
    }

    @Basic
    @Column(name = "PSMStatus")
    public String getPsmStatus() {
        return psmStatus;
    }

    public void setPsmStatus(String psmStatus) {
        this.psmStatus = psmStatus;
    }

    @Basic
    @Column(name = "UserServciceRoleDeleted")
    public Boolean getUserServciceRoleDeleted() {
        return userServciceRoleDeleted;
    }

    public void setUserServciceRoleDeleted(Boolean userServciceRoleDeleted) {
        this.userServciceRoleDeleted = userServciceRoleDeleted;
    }

    @Basic
    @Column(name = "UserDeleted")
    public Boolean getUserDeleted() {
        return userDeleted;
    }

    public void setUserDeleted(Boolean userDeleted) {
        this.userDeleted = userDeleted;
    }

    @Basic
    @Column(name = "ServiceProviderDeleted")
    public Boolean getServiceProviderDeleted() {
        return serviceProviderDeleted;
    }

    public void setServiceProviderDeleted(Boolean serviceProviderDeleted) {
        this.serviceProviderDeleted = serviceProviderDeleted;
    }

    @Basic
    @Column(name = "RoleDeleted")
    public Boolean getRoleDeleted() {
        return roleDeleted;
    }

    public void setRoleDeleted(Boolean roleDeleted) {
        this.roleDeleted = roleDeleted;
    }

    @Basic
    @Column(name = "ProviderServiceMappingDeleted")
    public Boolean getProviderServiceMappingDeleted() {
        return providerServiceMappingDeleted;
    }

    public void setProviderServiceMappingDeleted(Boolean providerServiceMappingDeleted) {
        this.providerServiceMappingDeleted = providerServiceMappingDeleted;
    }

    @Basic
    @Column(name = "isInbound")
    public Boolean getInbound() {
        return isInbound;
    }

    public void setInbound(Boolean inbound) {
        isInbound = inbound;
    }

    @Basic
    @Column(name = "isOutbound")
    public Boolean getOutbound() {
        return isOutbound;
    }

    public void setOutbound(Boolean outbound) {
        isOutbound = outbound;
    }

    @Basic
    @Column(name = "blockid")
    public Integer getBlockid() {
        return blockid;
    }

    public void setBlockid(Integer blockid) {
        this.blockid = blockid;
    }

    @Basic
    @Column(name = "blockname")
    public String getBlockname() {
        return blockname;
    }

    public void setBlockname(String blockname) {
        this.blockname = blockname;
    }

    @Basic
    @Column(name = "villageid")
    public String getVillageid() {
        return villageid;
    }

    public void setVillageid(String villageid) {
        this.villageid = villageid;
    }

    @Basic
    @Column(name = "villagename")
    public String getVillagename() {
        return villagename;
    }

    public void setVillagename(String villagename) {
        this.villagename = villagename;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, usrMappingId, name, userName, serviceId, serviceName, isNational, stateId, stateName, workingDistrictId, workingDistrictName, workingLocationId, serviceProviderId, locationName, workingLocationAddress, roleId, roleName, providerServiceMapId, agentId, psmStatusId, psmStatus, userServciceRoleDeleted, userDeleted, serviceProviderDeleted, roleDeleted, providerServiceMappingDeleted, isInbound, isOutbound, blockid, blockname, villageid, villagename);
    }

}
