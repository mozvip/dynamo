FROM openjdk:8
RUN apt-get -y update
RUN apt-get install -y apt-utils nginx mediainfo transmission-daemon libyaml-dev python-dev python-pip
#&& apt-get install -y software-properties-common 
#RUN add-apt-repository ppa:jcfp/ppa && apt-get -y update 
RUN pip install --upgrade pip && pip install beets
RUN wget https://github.com/sabnzbd/sabnzbd/releases/download/2.0.0/SABnzbd-2.0.0-src.tar.gz
RUN tar xzvf ./SABnzbd-2.0.0-src.tar.gz
RUN wget https://guigui.tech/downloads/dynamo-core-0.0.1-SNAPSHOT.jar
RUN java -jar dynamo-core-0.0.1-SNAPSHOT.jar