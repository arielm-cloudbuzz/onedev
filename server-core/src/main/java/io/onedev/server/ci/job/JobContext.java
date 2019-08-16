package io.onedev.server.ci.job;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jgit.lib.ObjectId;

import io.onedev.server.util.JobLogger;
import io.onedev.server.util.patternset.PatternSet;

public abstract class JobContext {
	
	private final String projectName;
	
	private final File projectGitDir;
	
	private final String environment;
	
	private final File serverWorkspace;
	
	private final Map<String, String> envVars;
	
	private final List<String> commands;
	
	private final boolean retrieveSource;
	
	private final List<SubmoduleCredential> submoduleCredentials;
	
	private final ObjectId commitId;
	
	private final Collection<CacheSpec> cacheSpecs; 
	
	private final PatternSet collectFiles;
	
	private final int cacheTTL;
	
	private final JobLogger logger;	
	
	private final Collection<String> allocatedCaches = new HashSet<>();
	
	private final Map<String, Integer> cacheCounts = new ConcurrentHashMap<>();
	
	public JobContext(String projectName, File projectGitDir, String environment, 
			File workspace, Map<String, String> envVars, List<String> commands, 
			boolean retrieveSource, List<SubmoduleCredential> submoduleCredentials, ObjectId commitId,  
			Collection<CacheSpec> caches, PatternSet collectFiles, int cacheTTL, JobLogger logger) {
		this.projectName = projectName;
		this.projectGitDir = projectGitDir;
		this.environment = environment;
		this.serverWorkspace = workspace;
		this.envVars = envVars;
		this.commands = commands;
		this.retrieveSource = retrieveSource;
		this.submoduleCredentials = submoduleCredentials;
		this.commitId = commitId;
		this.cacheSpecs = caches;
		this.collectFiles = collectFiles;
		this.cacheTTL = cacheTTL;
		this.logger = logger;
	}

	public String getProjectName() {
		return projectName;
	}

	public File getProjectGitDir() {
		return projectGitDir;
	}

	public String getEnvironment() {
		return environment;
	}

	public File getServerWorkspace() {
		return serverWorkspace;
	}

	public Map<String, String> getEnvVars() {
		return envVars;
	}

	public List<String> getCommands() {
		return commands;
	}

	public ObjectId getCommitId() {
		return commitId;
	}

	public boolean isRetrieveSource() {
		return retrieveSource;
	}
	
	public List<SubmoduleCredential> getSubmoduleCredentials() {
		return submoduleCredentials;
	}

	public Collection<CacheSpec> getCacheSpecs() {
		return cacheSpecs;
	}

	public PatternSet getCollectFiles() {
		return collectFiles;
	}

	public JobLogger getLogger() {
		return logger;
	}
	
	public int getCacheTTL() {
		return cacheTTL;
	}

	public Collection<String> getAllocatedCaches() {
		return allocatedCaches;
	}

	public Map<String, Integer> getCacheCounts() {
		return cacheCounts;
	}

	public abstract void notifyJobRunning();
	
}
