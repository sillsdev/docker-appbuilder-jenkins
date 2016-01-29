import jenkins.model.*

def jobName = "Job-Wrapper-Seed"
def configXml = """\
<?xml version='1.0' encoding='UTF-8'?>
<project>
  <actions/>
  <description>Create new jobs from groovy scripts in appbuilder-ci-scripts project</description>
  <logRotator class="hudson.tasks.LogRotator">
    <daysToKeep>-1</daysToKeep>
    <numToKeep>10</numToKeep>
    <artifactDaysToKeep>-1</artifactDaysToKeep>
    <artifactNumToKeep>-1</artifactNumToKeep>
  </logRotator>
  <keepDependencies>false</keepDependencies>
  <properties/>
  <scm class="hudson.plugins.git.GitSCM" plugin="git@2.4.0">
    <configVersion>2</configVersion>
    <userRemoteConfigs>
      <hudson.plugins.git.UserRemoteConfig>
        <url>JOB_WRAPPER_GIT_URL</url>
        <credentialsId>appbuilder-buildagent</credentialsId>
      </hudson.plugins.git.UserRemoteConfig>
    </userRemoteConfigs>
    <branches>
      <hudson.plugins.git.BranchSpec>
        <name>*/JOB_WRAPPER_GIT_BRANCH</name>
      </hudson.plugins.git.BranchSpec>
    </branches>
    <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
    <submoduleCfg class="list"/>
    <extensions/>
  </scm>
  <canRoam>true</canRoam>
  <disabled>false</disabled>
  <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
  <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
  <triggers>
    <com.cloudbees.jenkins.plugins.BitBucketTrigger plugin="bitbucket@1.1.2">
      <spec></spec>
    </com.cloudbees.jenkins.plugins.BitBucketTrigger>
  </triggers>
  <concurrentBuild>false</concurrentBuild>
  <builders>
    <javaposse.jobdsl.plugin.ExecuteDslScripts plugin="job-dsl@1.38">
      <targets>groovy/*.groovy</targets>
      <usingScriptText>false</usingScriptText>
      <ignoreExisting>false</ignoreExisting>
      <removedJobAction>DELETE</removedJobAction>
      <removedViewAction>DELETE</removedViewAction>
      <lookupStrategy>JENKINS_ROOT</lookupStrategy>
      <additionalClasspath></additionalClasspath>
    </javaposse.jobdsl.plugin.ExecuteDslScripts>
  </builders>
  <publishers/>
  <buildWrappers/>
</project>
"""
def gitUrl = System.getenv('APPBUILDER_JOB_WRAPPER_GIT_URL');
println 'Starting debug'
for (p in Jenkins.instance.allItems) {
    if (!p.name.startsWith(jobName)) continue
    println '- ' + p.name

}
if (gitUrl?.trim()) {
    configXml = configXml.replaceAll('JOB_WRAPPER_GIT_URL', gitUrl);
}
def gitBranch = System.getenv('APPBUILDER_JOB_WRAPPER_GIT_BRANCH');
if (gitBranch?.trim()) {
	configXml = configXml.replaceAll('JOB_WRAPPER_GIT_BRANCH', gitBranch);
}

def xmlStream = new ByteArrayInputStream(configXml.getBytes())
Jenkins.instance.createProjectFromXML(jobName, xmlStream)
