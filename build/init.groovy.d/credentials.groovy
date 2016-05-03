import jenkins.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.common.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*
import org.jenkinsci.plugins.plaincredentials.*
import org.jenkinsci.plugins.plaincredentials.impl.*
import hudson.plugins.sshslaves.*
import hudson.util.Secret
import org.apache.commons.fileupload.*
import org.apache.commons.fileupload.disk.*
import org.apache.commons.io.FileUtils
import java.nio.file.Files

domain = Domain.global()
credentialStore = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()

def appbuilderFolder = new File("/usr/share/jenkins/secrets/jenkins/build/appbuilder_ssh")
if (appbuilderFolder.exists()) {
    privateKeyAppBuilderFile = new File("/usr/share/jenkins/secrets/jenkins/build/appbuilder_ssh/id_rsa")
    privateKeyAppBuilder = new BasicSSHUserPrivateKey(
    CredentialsScope.GLOBAL,
    'appbuilder-buildagent',
    'AppBuilder',
    new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(
            FileUtils.readFileToString(privateKeyAppBuilderFile, "iso-8859-1")),
    "", //Passphrase
    "AppBuilder SSH Key" // Description
    )
    credentialStore.addCredentials(domain, privateKeyAppBuilder)
}

privateKeyBuildEngineFile = new File("/usr/share/jenkins/secrets/buildengine_api/ssh/id_rsa")
privateKeyBuildEngine = new BasicSSHUserPrivateKey(
    CredentialsScope.GLOBAL,
    'buildengine',
    'BuildEngine',
    new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(
            FileUtils.readFileToString(privateKeyBuildEngineFile, "iso-8859-1")),
    "", //Passphrase
    "BuildEngine SSH Key" // Description
)
credentialStore.addCredentials(domain, privateKeyBuildEngine)

factory = new DiskFileItemFactory();
def googlePlayBuildFolder = new File("/usr/share/jenkins/secrets/jenkins/build/google_play_store")
if (googlePlayBuildFolder.exists()) {
    new File("/usr/share/jenkins/secrets/jenkins/build/google_play_store").eachDir() { dir ->
        store = dir.getName()
        txtFile = ~/.*\.txt/
        dir.eachFileMatch(txtFile) { file ->
                name = file.getName().split("\\.")[0]
                secretText = new StringCredentialsImpl(
                        CredentialsScope.GLOBAL,
                        store+"-"+name,
                        store+" "+name,
                        Secret.fromString(file.text))
                credentialStore.addCredentials(domain, (Credentials) secretText)
        }

        ksFile = ~/.*\.keystore/
        dir.eachFileMatch(ksFile) { file ->
                name = file.getName().split("\\.")[0]
                dfi = factory.createItem("", "application/octet-stream", false, file.getName())
                out = dfi.getOutputStream()
                java.nio.file.Files.copy(file.toPath(), out);
                println "File name=${dfi.getName()}, size=${dfi.getSize()}"
                secretFile = new FileCredentialsImpl(
                        CredentialsScope.GLOBAL,
                        store+"-ks",
                        store+" "+file.getName(),
                        dfi,
                        "",
                        "")
                credentialStore.addCredentials(domain, (Credentials) secretFile)
        }
    }    
}
def googlePlayPublishFolder = new File("/usr/share/jenkins/secrets/jenkins/publish/google_play_store")
if (googlePlayPublishFolder.exists()) {
    new File("/usr/share/jenkins/secrets/jenkins/publish/google_play_store").eachDir() { dir ->
        store = dir.getName()

        playstoreApiJson = new File(dir, "playstore_api.json")
        dfi = factory.createItem("", "application/octet-stream", false, playstoreApiJson.getName())
        out = dfi.getOutputStream()
        java.nio.file.Files.copy(playstoreApiJson.toPath(), out);
        playstoreApiJsonSecretFile = new FileCredentialsImpl(
            CredentialsScope.GLOBAL,
            store+"-playstore-api-json",
            store+" Playstore API JSON",
            dfi,
            "",
            "")
        credentialStore.addCredentials(domain, (Credentials) playstoreApiJsonSecretFile)
    }    
}
// Delete all the secret files after importing
new File("/usr/share/jenkins/secrets").deleteDir()
