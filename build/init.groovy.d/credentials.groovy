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

privateKeyFile = new File("/usr/share/jenkins/secrets/jenkins_ssh/id_rsa")
privateKey = new BasicSSHUserPrivateKey(
CredentialsScope.GLOBAL,
'appbuilder-buildagent',
'AppBuilderBuildAgent',
new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(
	FileUtils.readFileToString(privateKeyFile, "iso-8859-1")),
"", //Passphrase
"AppBuilder BuildAgent SSH Key" // Description
)
credentialStore.addCredentials(domain, privateKey)

new File("/usr/share/jenkins/secrets/google_play_store").eachDir() { dir ->
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
		factory = new DiskFileItemFactory();
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

// Delete all the secret files after importing
new File("/usr/share/jenkins/secrets").deleteDir()
