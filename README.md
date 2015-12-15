docker-appbuilder-jenkins
=================

Jenkins Continous Integration build server for building mobile apps.

Essentially, this project has two components; a Dockerfile configuring a main,
master Jenkins build server, and a Dockerfile configuring slave build
containers. The slave Dockerfile is located in a separate repository, at
[https://bitbucket.org/silintl/docker-appbuilder-agent](https://bitbucket.org/silintl/docker-appbuilder-agent).

This repository contains the master build server Dockerfile, configuration
files specific to Codeship (a Continous Integration as a Service provider), and
a [vagrant-boot2docker](https://github.com/silinternational/vagrant-boot2docker)
environment for local development and testing.

Vagrantfile
-----------

The include Vagrantfile spins up a boot2docker box, builds the docker image, 
and runs the `docker-compose` on the included docker-compose.yml to build and
start the Jenkins master and a slave agent.

After you have installed virtualbox and vagrant just run:

    vagrant up

The Jenkins master server will be accessible at
[http://192.168.70.241](http://192.168.70.241).

If you change the Dockerfile, you will need to run these commands:

    vagrant ssh
    cd /vagrant
    docker-compose stop
    docker-compose rm -f
    docker-compose build
    docker-compose up -d

If you find yourself doing a `vagrant destroy` and then `vagrant up` repeatedly,
you can define the environmental variable `DOCKER_IMAGEDIR_PATH` in your shell,
giving it the value of an absolute filesystem path to folder. The Vagrantfile
will then cache the images it pulls from the internet into that folder, so that
it can load them directly from the disk every `vagrant reload`, rather than
pulling them over the network.

Jenkins Configuration
---------------------

Configuration for a Jenkins server is stored within xml files on the local disk,
all beneath one directory know as the "Jenkins Home".

Plugins are installed as part of the Docker build process.  The complete list of
plugins (including dependencies) must be in the file build/plugins.txt

Any configuration changes from the base image are done using groovy scripts
during startup.  These groovy scripts are located in build/init.groovy.d.
See the docs at
[Configuring Jenkins upon start up](https://wiki.jenkins-ci.org/display/JENKINS/Configuring+Jenkins+upon+start+up)
and [Overview: Jenkins main module API](http://javadoc.jenkins-ci.org/).


Some configuration is not exposed out to modification by groovy.  In this case,
it might be possible to include the preformatted xml configuration in
build/jenkins_home.

NOTE: First, attempt to find a way to affect the configuration using scripting.
Only place configuration in build/jenkins_home as a last resort.
