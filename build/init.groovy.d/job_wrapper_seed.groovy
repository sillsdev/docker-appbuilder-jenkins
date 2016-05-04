import jenkins.model.*
import hudson.model.*
import javax.xml.transform.stream.*

def buildJobName = "Build-Wrapper-Seed"
def publishJobName = "Publish-Wrapper-Seed"
def publishFiles = "groovy/publish_*.groovy"
def buildFiles = "groovy/build_*.groovy"
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
        <credentialsId>buildengine</credentialsId>
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
      <targets>GROOVY_FILES</targets>
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
def gitUrl = System.getenv('BUILD_ENGINE_REPO_URL');
def gitUser = System.getenv('BUILD_ENGINE_GIT_SSH_USER');

if (gitUser) {
  gitUrl = "ssh://" + gitUser + "@" + gitUrl.substring(6);
}
println "GitURL: " + gitUrl;

if (gitUrl?.trim()) {
    configXml = configXml.replaceAll('JOB_WRAPPER_GIT_URL', gitUrl);
}
def gitBranch = System.getenv('BUILD_ENGINE_REPO_BRANCH');
if (gitBranch?.trim()) {
	configXml = configXml.replaceAll('JOB_WRAPPER_GIT_BRANCH', gitBranch);
}

buildConfigXml = configXml.replaceAll('GROOVY_FILES', buildFiles);
def buildXmlStream = new ByteArrayInputStream(buildConfigXml.getBytes())
buildJob = Jenkins.instance.getItemByFullName(buildJobName, AbstractItem)
if (buildJob) {
  println "Updating job:" + buildJobName
  buildJob.updateByXml(new StreamSource(buildXmlStream))
} else {
  println "Creating job:" + buildJobName
  Jenkins.instance.createProjectFromXML(buildJobName, buildXmlStream)
}
publishConfigXml = configXml.replaceAll('GROOVY_FILES', publishFiles);
def publishXmlStream = new ByteArrayInputStream(publishConfigXml.getBytes())
publishJob = Jenkins.instance.getItemByFullName(publishJobName, AbstractItem)
if (publishJob) {
  println "Updating job:" + publishJobName
  publishJob.updateByXml(new StreamSource(publishXmlStream))
} else {
  println "Creating job:" + publishJobName
  Jenkins.instance.createProjectFromXML(publishJobName, publishXmlStream)
}
