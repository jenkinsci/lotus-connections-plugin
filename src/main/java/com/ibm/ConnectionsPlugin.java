package com.ibm;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.tasks.test.AggregatedTestResultAction;


import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;




/**
 * @author philrumble
 */
public class ConnectionsPlugin extends Notifier {
    private static final List<String> VALUES_REPLACED_WITH_NULL = Arrays.asList("", "(Default)", "(System Default)");

    private static final Logger LOGGER = Logger.getLogger(ConnectionsPlugin.class.getName());

    @DataBoundConstructor
    public ConnectionsPlugin() {
        LOGGER.info("ConnectionsPlugin");
    }


    public BuildStepMonitor getRequiredMonitorService() {
        LOGGER.info("getRequiredMonitorService");
        return BuildStepMonitor.BUILD;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        LOGGER.info("performing Lotus connections update");
        
        try {
            String newStatus = createStatusMessage(build);
            LOGGER.info("performing Lotus connections update with message " + newStatus);
            ((DescriptorImpl) getDescriptor()).updateConnections(newStatus);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to update Lotus Connections Status.", e);
        }
        LOGGER.info("performed Lotus connections update");
        return true;
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
            LOGGER.info("There is an aggregated test result");
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

        
        
        
        
                ;
//        message += "<br>";
//        message += " This mesage was sent from a Jenkins Continuous Integration Server using the Lotus Connections Plugin by Phil Rumble prumble@au1.ibm.com";
        
//            int failedTests = build.getTestResultAction().getFailCount();
        LOGGER.info("message = " + message);
        return message;
//        String toblame = "";
//        try {
//            if (!build.getResult().equals(Result.SUCCESS)) {
//                toblame = getUserString(build);
//            }
//        } catch (Exception ignore) {
//        }
//        String tinyUrl = "";
//        if (shouldIncludeUrl()) {
//            String absoluteBuildURL = ((DescriptorImpl) getDescriptor()).getUrl() + build.getUrl();
//            try {
//                tinyUrl = createTinyUrl(absoluteBuildURL);
//            } catch (Exception e) {
//                tinyUrl = "?";
//            }
//        }
//        return String.format("%s%s:%s $%d - %s", toblame, result, projectName, build.number, tinyUrl);
//        return String.format("%s (%d) : Tests Executed = %d Tests Passed = %d Tests Failed = %d Pass Rate = %d%",
//                projectName, 
//                build.number,
//                totalTests, 
//                passedTests,
//                failedTests,
//                passRate);
        
    }

    public DescriptorImpl getDescriptor() {
        // see Descriptor javadoc for more about what a descriptor is.
        return (DescriptorImpl)super.getDescriptor();
    }

    
//  //Save the form data
//    @Override
//    public boolean configure(StaplerRequest req, JSONObject formData) {
//        envs.replaceBy(req.bindParametersToList(ConnectionsDescriptor.class, "env."));
//        save();
//        return true;
//    }
//
//    //repopulate the saved form data
//    @Override
//    public BuildWrapper newInstance(StaplerRequest req, JSONObject formData)
//            throws FormException {
//        return req.bindJSON(EnvSelectorBuildWrapper.class, formData);
////      return super.newInstance(req, formData);
//    }
    
    
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        private static final Logger LOGGER = Logger.getLogger(DescriptorImpl.class.getName());


        public DescriptorImpl() {
            super(ConnectionsPlugin.class);
            LOGGER.info("ConnectionsPublisher");
            load();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            

            username = formData.getString("username");
            password = formData.getString("password");
            connectionsUrl = formData.getString("connectionsUrl");
            
            LOGGER.fine("username = " + username);
            LOGGER.fine("password = " + password);
            LOGGER.fine("connectionsUrl = " + connectionsUrl);

            save();
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Lotus Connections Notifier";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        /**
         * Creates a new instance of {@link ConnectionsPublisher} from a submitted form.
         */
        public ConnectionsPlugin newInstance(StaplerRequest req) throws FormException {
            LOGGER.fine("New instance of ConnectionsPlugin for a job");
            return new ConnectionsPlugin();
        }
        
//        @Override
//        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
//            //return req.bindJSON(this.getClass(), formData);
//            
//            req.bindParameters(this, "lotusconnections.");
//            load();
//            return super.newInstance(req, formData);
//////            save();
////////            if (formData.has("twitterid")) {
////////                return req.bindJSON(UserTwitterProperty.class, formData);
//////            return super.newInstance(req, formData);
//        }

        public void updateConnections(String message) throws Exception {
            try
            {
                LOGGER.info("updateConnections");
               
                Poster poster = new Poster();
                LOGGER.info("Connecting to Connections Server --  " + this.connectionsUrl);
                poster.postStatus(connectionsUrl, username, password, message);

                LOGGER.info("Updated Connection status: " + message);
            }
            catch (Exception e)
            {
                LOGGER.info("updateConnections - Exception caught.");
                throw e;
            }
        }
        
        public String connectionsUrl;

        public String username;

        public String password;
        
        
        public String getConnectionsUrl() {
            return connectionsUrl;
        }
     
        public void setConnectionsUrl(String connectionsUrl) {
            LOGGER.info("setting connectionsUrl to " + connectionsUrl);
            this.connectionsUrl = connectionsUrl;
        }
     
        public String getUserName() {
            
            return username;
        }
     
        public void setUserName(String username) {
            LOGGER.info("setting username to " + username);
            this.username = username;
        }
     
        public String getPassword() {
            return password;
        }
     
        public void setPassword(String password) {
            LOGGER.info("setting password to " + password);
            this.password = password;
        }
    }
}
