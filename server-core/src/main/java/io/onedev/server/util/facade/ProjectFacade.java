package io.onedev.server.util.facade;

import javax.annotation.Nullable;

public class ProjectFacade extends EntityFacade {
	
	private static final long serialVersionUID = 1L;
	
	private final String name;
	
	private final String serviceDeskName;
	
	private final boolean issueManagement;
	
	private final Long roleId;
	
	private final Long parentId;
	
	public ProjectFacade(Long id, String name, @Nullable String serviceDeskName, 
			boolean issueManagement, @Nullable Long roleId, @Nullable Long parentId) {
		super(id);
		this.name = name;
		this.serviceDeskName = serviceDeskName;
		this.issueManagement = issueManagement;
		this.roleId = roleId;
		this.parentId = parentId;
	}

	public String getName() {
		return name;
	}

	public boolean isIssueManagement() {
		return issueManagement;
	}

	@Nullable
	public String getServiceDeskName() {
		return serviceDeskName;
	}

	@Nullable
	public Long getDefaultRoleId() {
		return roleId;
	}

	@Nullable
	public Long getParentId() {
		return parentId;
	}

}
