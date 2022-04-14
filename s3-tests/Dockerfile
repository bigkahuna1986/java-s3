FROM ubuntu
ENV TZ=Etc/UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN DEBIAN_FRONTEND=noninteractive apt-get update && apt-get -y upgrade && apt-get -y install bash vim less git python3-pip build-essential python-dev python3-venv tzdata sudo && mkdir /tests
RUN git clone "https://github.com/ceph/s3-tests.git" /tests 
RUN cd /tests && ./bootstrap
