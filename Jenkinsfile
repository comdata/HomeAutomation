pipeline {
    agent {
        docker {
            image 'maven:3.5.2-jdk-8' 
            args '-v /var/jenkins_home/.m2:/root/.m2' 
        }
    }
    stages {
	stage('Prepare') {
	    steps {
		sh 'apk update'
		sh 'apk add rsync'
	    } 
	}

        stage('Build') { 
            steps {
                sh 'mvn -B -DskipTests clean package' 
            }
        }
        stage('Deploy') {
            steps {
		
                sh 'scp -rp HomeAutomationUI/WebContent/* root@192.168.1.76:/var/lib/tomcat8/webapps/HomeAutomationUI/WebContent/'
            	sh 'scp -rp HomeAutomation/target/HomeAutomation-0.0.1-SNAPSHOT/WEB-INF/* root@192.168.1.76:/var/lib/tomcat8/webapps/HomeAutomation/WEB-INF/'
	    	sh 'ssh root@192.168.1.76 /etc/init.d/tomcat8 restart'
	    }
        }

    }
}
