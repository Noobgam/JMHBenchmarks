sudo apt-get update && \
sudo apt install openjdk-13-jdk unzip -y && \
sudo apt-get upgrade && \
wget https://services.gradle.org/distributions/gradle-5.0-bin.zip -P /tmp && \
sudo unzip -d /opt/gradle /tmp/gradle-*.zip && \
sudo echo "export GRADLE_HOME=/opt/gradle/gradle-5.0" >> /etc/profile.d/gradle.sh && \
sudo echo "export PATH=${GRADLE_HOME}/bin:${PATH}" >> /etc/profile.d/gradle.sh && \
sudo chmod +x /etc/profile.d/gradle.sh && \
source /etc/profile.d/gradle.sh