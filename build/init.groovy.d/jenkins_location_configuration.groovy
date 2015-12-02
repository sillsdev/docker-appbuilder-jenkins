jlc = new jenkins.model.JenkinsLocationConfiguration();
jenkinsUrl = System.getenv('APPBUILDER_JENKINS_URL');
if (jenkinsUrl?.trim()) {
    jlc.setUrl(jenkinsUrl);
}

jenkinsAdminAddress = System.getenv('APPBUILDER_JENKINS_ADMIN_EMAIL');
if (jenkinsAdminAddress?.trim()) {
    jlc.setAdminAddress(jenkinsAdminAddress);
}
