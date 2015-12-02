FROM jenkins:1.609.2
MAINTAINER <Chris Hubbard> "chris_hubbard@sil.org"

USER root
RUN apt-get update && apt-get install -y \
      s3cmd vim

RUN curl https://raw.githubusercontent.com/silinternational/s3-expand/master/s3-expand > /usr/local/bin/s3-expand
RUN chmod a+x /usr/local/bin/s3-expand

COPY build/init.groovy.d/ /usr/share/jenkins/ref/init.groovy.d/
COPY build/jenkins_home/ ${JENKINS_HOME}

COPY build/plugins.txt /usr/share/jenkins/ref/
RUN /usr/local/bin/plugins.sh /usr/share/jenkins/ref/plugins.txt

ENTRYPOINT ["/usr/local/bin/s3-expand","/bin/tini","--","/usr/local/bin/jenkins.sh"]
