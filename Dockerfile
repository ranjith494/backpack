FROM geetharam/selenium-chrome-vnc:latest

RUN apt-get update && \
    apt-get install -y sudo curl openssh-client git wget     

RUN apt-get update && apt-get clean

ENV NODE_PATH /usr/local/lib/node_modules/

# Install Node
RUN   \
  wget -O - http://nodejs.org/dist/v0.10.29/node-v0.10.29-linux-x64.tar.gz \
  | tar xzf - --strip-components=1 --exclude="README.md" --exclude="LICENSE" \
  --exclude="ChangeLog" -C "/usr/local"

# Install NPM dependencies
RUN npm install -g grunt-cli
RUN npm install -g karma
RUN npm install -g grunt-karma
RUN npm install -g chromedriver
#RUN npm install -g phantomjs
#RUN npm install -g casperjs
#RUN npm install -g karma-phantomjs-launcher
RUN npm install -g protractor
RUN npm install -g jasmine-reporters
RUN webdriver-manager update

RUN  mkdir -p /opt/tests

COPY todo-spec.js /opt/tests/todo-spec.js
COPY conf.js /opt/tests/conf.js 
COPY runTest.sh /opt/tests/runTest.sh 
RUN npm config set prefix /usr/local/lib/node_modules
RUN npm cache clear
#CMD /bin/bash
#CMD ["--net=host"]
#ENTRYPOINT ["protractor","/opt/tests/conf.js"]
CMD protractor /opt/tests/conf.js 
#RUN docker run prot_tests
#EXPOSE 4444
