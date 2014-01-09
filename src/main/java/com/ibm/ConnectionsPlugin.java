package com.ibm;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.AggregatedTestResultAction;
import hudson.util.FormValidation;
import hudson.util.Secret;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;



/**
 * @author philrumble
 */
public class ConnectionsPlugin extends Notifier {

    public final String connectionsUrl;
    public final String connectionsUser;
    public final String connectionsPassword;
    public final Boolean enablestatus;
    public final Boolean enableforum;
    public final String communityuuid;
    private final Logger logger = Logger.getLogger("ConnectionsPlugin");
    
    
    
    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public ConnectionsPlugin(String connectionsUrl,
            String connectionsUser,
            String connectionsPassword,
            Boolean enablestatus,
            Boolean enableforum,
            String communityuuid) {
        logger.info("ConnectionsPlugin");
        this.connectionsUrl = connectionsUrl;
        this.connectionsUser = connectionsUser;
        this.connectionsPassword = connectionsPassword;
        this.enablestatus = enablestatus;
        this.enableforum = enableforum;
        this.communityuuid = communityuuid;
    }

    public BuildStepMonitor getRequiredMonitorService() {
//        LOGGER.info("getRequiredMonitorService");
        return BuildStepMonitor.BUILD;
    }
    
//    /**
//     * We'll use this from the <tt>config.jelly</tt>.
//     */
//    public String getConnectionsUrl() {
//		return connectionsUrl;
//	}
//	public String getConnectionsUser() {
//		return connectionsUser;
//	}
//	public String getConnectionsPassword() {
//		return connectionsPassword;
//	}
//	public Boolean getEnableStatus() {
//		return enablestatus;
//	}
//	public Boolean getEnableForum() {
//        return enableforum;
//    }
//
//	public String getCommunityUuid() {
//		return communityuuid;
//	}


	/**
	 * This is where you 'build' the project.
	 */
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
    	
        listener.getLogger().println("Performing Lotus Connections Magic");
    	final String url = connectionsUrl;
    	final String user = connectionsUser;
    	final Secret password = Secret.fromString(connectionsPassword);
    	
    	listener.getLogger().println("url = " + url);
    	listener.getLogger().println("user = " + user);
    	listener.getLogger().println("password = " + password);
    	if( ! validatePluginConfiguration(url, user, Secret.toString( password ) ) ) {
			listener.getLogger().println("Please configure Lotus Connections Plugin");
			return false;
		}
		
//    	Logger logger = Logger.getLogger("ConnectionsPlugin");
    	logger.info("url = " + url);
    	logger.info("user = " + user);
    	logger.info("password = " + password);
    	
    	com.ibm.Poster poster = new com.ibm.Poster();
    	
    	try {
    	    

        	if (communityuuid != null)
        	{
        	    String forumTitle = createForumTitle(build);
                String forumMessage = createForumMessage(build);
                
        	    logger.info("Posting to community forum");
        	    poster.postForumTopic(url,
                      user,
                      connectionsPassword,
                      communityuuid,
                        forumTitle,
                        forumMessage);
        	}
        	else
        	{
        	    logger.info("Not posting to community forum");
        	}
        	
        	if (enablestatus)
        	{
        	    logger.info("Posting to user's status");
        	    String message = createStatusMessage(build);
        	    poster.postStatus(url, user, connectionsPassword, message);
        	}
        	else
        	{
                logger.info("Not posting to user's status");
            }
    	}
    	catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to update Lotus Connections Status.", e);
        }
        logger.info("performed Lotus connections update");
        
        return true;
    }
    
    private String createForumTitle(AbstractBuild<?, ?> build) {
        String projectName = build.getProject().getName();
        String result = build.getResult().toString();
        String title = String.format("%s (%d) : %s ", 
                projectName, 
                build.number,
                result);
        AggregatedTestResultAction atra = build.getAggregatedTestResultAction();
        if (null != atra)
        {
            logger.info("There is an aggregated test result");
            // Tests Executed = 702 Tests Passed = 518 Tests Failed = 184 Pass Rate = 73%
            int totalTests = build.getAggregatedTestResultAction().getTotalCount();
            int passedTests = build.getAggregatedTestResultAction().getTotalCount();
            int failedTests = build.getAggregatedTestResultAction().getTotalCount();
            int passRate = (passedTests/totalTests) * 100;
            title = String.format("%s (%d) : Tests Executed = %d Tests Passed = %d Tests Failed = %d Pass Rate = %d%",
                    projectName, 
                    build.number,
                    totalTests, 
                    passedTests,
                    failedTests,
                    passRate);

        }
        else
        {
            AbstractTestResultAction tra = build.getTestResultAction();
            if (null != tra)
            {
                int totalTests = tra.getTotalCount();
                int failedTests = tra.getFailCount();
                int passedTests = totalTests - failedTests - tra.getSkipCount();
               
                float in1 = (100*passedTests);
                float passRate = (in1/totalTests);
                title = String.format("%s (%d) : Tests Executed = %d Tests Passed = %d Tests Failed = %d Pass Rate = %.2f %%",
                        projectName, 
                        build.number,
                        totalTests, 
                        passedTests,
                        failedTests,
                        passRate);
            }
            
        }
        
        logger.info("title = " + title);
        return title;
    }

    private String createForumMessage(AbstractBuild<?, ?> build) {
        String projectName = build.getProject().getName();
        String result = build.getResult().toString();
        String message = String.format("%s (%d) : %s ", 
                projectName, 
                build.number,
                result);
        AggregatedTestResultAction atra = build.getAggregatedTestResultAction();
        if (null != atra)
        {
            logger.info("There is an aggregated test result");
            // Tests Executed = 702 Tests Passed = 518 Tests Failed = 184 Pass Rate = 73%
            int totalTests = build.getAggregatedTestResultAction().getTotalCount();
            int passedTests = build.getAggregatedTestResultAction().getTotalCount();
            int failedTests = build.getAggregatedTestResultAction().getTotalCount();
            int passRate = (passedTests/totalTests) * 100;
            message = String.format("%s (%d) : Tests Executed = %d Tests Passed = %d Tests Failed = %d Pass Rate = %d%",
                    projectName, 
                    build.number,
                    totalTests, 
                    passedTests,
                    failedTests,
                    passRate);

        }
        else
        {
            AbstractTestResultAction tra = build.getTestResultAction();
            if (null != tra)
            {
                int totalTests = tra.getTotalCount();
                int failedTests = tra.getFailCount();
                int passedTests = totalTests - failedTests - tra.getSkipCount();
               
                float in1 = (100*passedTests);
                float passRate = (in1/totalTests);
                message = String.format("%s (%d) : Tests Executed = %d Tests Passed = %d Tests Failed = %d Pass Rate = %.2f %%",
                        projectName, 
                        build.number,
                        totalTests, 
                        passedTests,
                        failedTests,
                        passRate);
            }
            
        }
        
        String rootUrl = Hudson.getInstance().getRootUrl();
        if (rootUrl != null) {
            message += " "+rootUrl + build.getUrl();
        }
        logger.info("message = " + message);
        return message;
    }
    
    private String createStatusMessage(AbstractBuild<?, ?> build) {
        String projectName = build.getProject().getName();
        String result = build.getResult().toString();
        String message = String.format("%s (%d) : %s ", 
                projectName, 
                build.number,
                result);
        AggregatedTestResultAction atra = build.getAggregatedTestResultAction();
        if (null != atra)
        {
            logger.info("There is an aggregated test result");
            // Tests Executed = 702 Tests Passed = 518 Tests Failed = 184 Pass Rate = 73%
            int totalTests = build.getAggregatedTestResultAction().getTotalCount();
            int passedTests = build.getAggregatedTestResultAction().getTotalCount();
            int failedTests = build.getAggregatedTestResultAction().getTotalCount();
            int passRate = (passedTests/totalTests) * 100;
            message = String.format("%s (%d) : Tests Executed = %d Tests Passed = %d Tests Failed = %d Pass Rate = %d%",
                    projectName, 
                    build.number,
                    totalTests, 
                    passedTests,
                    failedTests,
                    passRate);

        }
        else
        {
            AbstractTestResultAction tra = build.getTestResultAction();
            if (null != tra)
            {
                int totalTests = tra.getTotalCount();
                int failedTests = tra.getFailCount();
                int passedTests = totalTests - failedTests - tra.getSkipCount();
               
                float in1 = (100*passedTests);
                float passRate = (in1/totalTests);
                message = String.format("%s (%d) : Tests Executed = %d Tests Passed = %d Tests Failed = %d Pass Rate = %.2f %%",
                        projectName, 
                        build.number,
                        totalTests, 
                        passedTests,
                        failedTests,
                        passRate);
            }
            
        }
        
        String rootUrl = Hudson.getInstance().getRootUrl();
        if (rootUrl != null) {
            message += " "+rootUrl + build.getUrl();
        }

        logger.info("message = " + message);
        return message;
        
    }

	private boolean validatePluginConfiguration(
			final String url,
			final String user, 
			final String password) {
		
		if( url == null || user == null || password == null || 
			url.isEmpty() || user.isEmpty() || password.isEmpty() ) {
			return false;
		}
		return true;
	}

    /**
     * Overridden for better type safety.
     * If your plugin doesn't really define any property on Descriptor,
     * you don't have to do this. 
     */
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

	/**
     * Descriptor for {@link NexusMetadataBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>src/main/resources/hudson/plugins/hello_world/NexusMetadataBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher>  {
       
//        public DescriptorImpl() {
//            load();
//        }
        


        /**
         * To persist global configuration information,
         * simply store it in a field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */

//        /**
//         * Performs on-the-fly validation of the form field 'connectionsurl'.
//         *
//         * @param value
//         *      This parameter receives the value that the user has typed.
//         * @return
//         *      Indicates the outcome of the validation. This is sent to the browser.
//         */
//        public FormValidation doCheckconnectionsurl(@QueryParameter String value)
//                throws IOException, ServletException {
//            if (value.length() == 0)
//                return FormValidation.error("Please set a connectionsurl");
//            if (value.length() < 4)
//                return FormValidation.warning("Isn't the connectionsurl too short?");
//            return FormValidation.ok();
//        }
//        public FormValidation doCheckValue(@QueryParameter String value)
//                throws IOException, ServletException {
//            if (value.length() == 0)
//                return FormValidation.error("Please set a value");
//            if (value.length() < 4)
//                return FormValidation.warning("Isn't the connectionsurl too short?");
//            return FormValidation.ok();
//        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Lotus Connections Notifications";
        }

    }
}

