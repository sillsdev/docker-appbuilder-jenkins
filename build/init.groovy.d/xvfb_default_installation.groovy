import jenkins.model.Jenkins
import org.jenkinsci.plugins.xvfb.*

def installation = new XvfbInstallation('default', '', null)

Jenkins.getInstance()
       .getDescriptorByType(Xvfb.XvfbBuildWrapperDescriptor.class)
       .setInstallations(installation)
