FROM gitpod/workspace-mysql
                    
USER gitpod

# Install custom tools, runtime, etc. using apt-get
# For example, the command below would install "bastet" - a command line tetris clone:
#
# RUN sudo apt-get -q update && #     sudo apt-get install -yq bastet && #     sudo rm -rf /var/lib/apt/lists/*
#
# More information: https://www.gitpod.io/docs/42_config_docker/

USER root
# Install custom tools, runtime, etc.
RUN apt-get update && apt-get install -y \
        mc mosquitto \
    && apt-get clean && rm -rf /var/cache/apt/* && rm -rf /var/lib/apt/lists/* && rm -rf /tmp/*
    
RUN echo "mysql -e \"CREATE DATABASE HA\"" >> ~/.bashrc

USER gitpod
# Apply user-specific settings
#ENV ...

# Give back control
USER root
