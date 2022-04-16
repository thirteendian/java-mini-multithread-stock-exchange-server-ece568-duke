# pull from base image
FROM gradle:7.4.1-jdk17

# add all files
ADD . /app

# define working directory
WORKDIR /app

RUN chmod +x scripts/runserver.sh
RUN gradle clean
RUN gradle resolveDependencies
RUN gradle assemble


