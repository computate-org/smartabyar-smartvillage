FROM quay.io/computateorg/smartvillage-traffic-light-controller-sumo:computate-api

MAINTAINER Christopher Tate <computate@computate.org>

ENV APP_DEPENDENCIES="java-17-openjdk-devel maven" \
  JAVA_HOME=/usr/lib/jvm/java-17-openjdk \
  PATH="${PATH}:/usr/local/opt/maven"

USER root

RUN yum install -y ${APP_DEPENDENCIES}

RUN install -d /usr/local/opt/maven/ && \
  curl https://dlcdn.apache.org/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.tar.gz -o /tmp/apache-maven-3.9.5-bin.tar.gz && \
  tar xvf /tmp/apache-maven-3.9.5-bin.tar.gz --strip-components=1 -C /usr/local/opt/maven/

RUN install -d /usr/local/src/smartabyar-smartvillage
COPY . /usr/local/src/smartabyar-smartvillage
RUN git clone https://github.com/computate-org/computate.git /usr/local/src/computate

RUN git clone https://github.com/computate-org/computate-base.git /usr/local/src/computate-base
RUN git clone https://github.com/computate-org/computate-search.git /usr/local/src/computate-search
RUN git clone https://github.com/computate-org/computate-vertx.git /usr/local/src/computate-vertx
RUN git clone https://github.com/computate-org/smartabyar-smartvillage-static.git /usr/local/src/smartabyar-smartvillage-static
RUN git clone https://github.com/computate-org/smartvillage-platform.git /usr/local/src/smartvillage-platform
WORKDIR /usr/local/src/computate-base
RUN mvn clean install -DskipTests -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true
WORKDIR /usr/local/src/computate-search
RUN mvn clean install -DskipTests -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true
WORKDIR /usr/local/src/computate-vertx
RUN mvn clean install -DskipTests -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true
WORKDIR /usr/local/src/smartvillage-platform
RUN mvn clean install -DskipTests -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true

WORKDIR /usr/local/src/smartabyar-smartvillage
RUN mvn clean install -DskipTests -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true
RUN cp /usr/local/src/smartabyar-smartvillage/target/*.jar /usr/local/src/smartabyar-smartvillage/app.jar
RUN cp -r /usr/local/src/smartabyar-smartvillage/src/main/resources/webroot/ /usr/local/src/smartabyar-smartvillage-static/
CMD java $JAVA_OPTS -cp .:* org.computate.smartvillage.enus.vertx.MainVerticle
