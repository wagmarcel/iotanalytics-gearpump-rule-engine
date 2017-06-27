FROM jamesdbloom/docker-java8-maven

RUN apt-get update -qq && apt-get install -y build-essential
RUN apt-get install -y python-setuptools

RUN easy_install poster
RUN easy_install requests

WORKDIR /app