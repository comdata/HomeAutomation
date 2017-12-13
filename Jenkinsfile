pipeline {
    agent {
        docker {
            image 'maven:3.5.2-jdk-8-alpine' 
            args '-v /var/jenkins_home/.m2:/root/.m2' 
        }
    }
    stages {
	stage('Prepare') {
	    steps {
		sh 'apk update'
		sh 'apk add rsync openssh mariadb mariadb-client'
		sh '/etc/init.d/mariadb start'
		sh 'mysql -u root -e "CREATE DATABASE HA;"'
		sh 'mysql -u root HA < HomeAutomation/WebContent/WEB-INF/log4j.sql'
	    } 
	}

        stage('Build') { 
            steps {
                sh 'mvn -B -DskipTests clean package' 
            }
        }
        stage('Deploy') {
            steps {
		
                sh 'rsync -auv HomeAutomationUI/WebContent/* root@192.168.1.76:/var/lib/tomcat8/webapps/HomeAutomationUI/WebContent/'
            	sh 'rsync -auv HomeAutomation/target/HomeAutomation-0.0.1-SNAPSHOT/WEB-INF/* root@192.168.1.76:/var/lib/tomcat8/webapps/HomeAutomation/WEB-INF/'
	    	sh 'ssh root@192.168.1.76 /etc/init.d/tomcat8 restart'
	    }
        }

    }
}
