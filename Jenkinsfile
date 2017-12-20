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
		sh 'apk add rsync openssh mariadb mariadb-client openrc git'
		sh 'mysql_install_db --user=mysql --rpm'
		sh '/usr/bin/mysqld_safe &'
		sh 'sleep 5' // for mysql to startup
		sh 'mysql -u root -e "CREATE DATABASE HA;"'
		sh 'mysql -u root HA < HomeAutomation/WebContent/WEB-INF/log4j.sql'
	    } 
	}

	stage('Build dependencies') {
	    steps {
		sh 'rm -rf obera-base zwave'
		sh 'git clone https://github.com/oberasoftware/obera-base.git'
		sh 'cd obera-base && mvn -DskipTests install'
		sh 'git clone https://github.com/comdata/zwave.git'
		sh 'cd zwave && mvn -DskipTests install'
		sh 'mvn -DskipTests install'
	    }
	}
        stage('Build') { 
            parallel {
		 stage('Build Backend') {
			steps {
				sh 'cd HomeAutomationBase && mvn -B clean install'
                		sh 'cd ..'
				sh 'cd HomeAutomation && mvn -B clean package'
				sh 'cd ..'
				junit '**/target/surefire-reports/**/*.xml'  
            		}
		}
		stage('Build Frontend') {
            		steps {
                		sh 'cd HomeAutomationUI && mvn -B clean package'
            		}
		}
	    }
        }

        stage('Deploy') {
            steps {
		
                sh 'rsync -auv HomeAutomationUI/WebContent/* root@192.168.1.76:/var/lib/tomcat8/webapps/HomeAutomationUI'
            	sh 'rsync -auv HomeAutomation/target/HomeAutomation-0.0.1-SNAPSHOT/WEB-INF/* root@192.168.1.76:/var/lib/tomcat8/webapps/HomeAutomation/WEB-INF/'
	    	sh 'ssh root@192.168.1.76 /etc/init.d/tomcat8 restart'
	    }
        }

    }
}
