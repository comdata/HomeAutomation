pipeline {
    agent {
        docker {
            image 'maven:3.6.1-jdk-8-alpine' 
            args '-v $HOME/.m2:/root/.m2 -v /root/.ssh:/root/.ssh' 
        }
    }
    triggers { upstream(upstreamProjects: 'comdata/HomeAutomationBase,comdata/olingo-jpa-processor-v4', threshold: hudson.model.Result.SUCCESS)}
    
    stages {
		stage('Prepare') {
		    steps {
			sh 'apk update'
			sh 'apk add nodejs rsync graphviz openssh mariadb mariadb-client openrc git'
			sh 'mysql_install_db --user=root'
			//sh 'rm /var/lib/mysql/ib_logfile0 /var/lib/mysql/ib_logfile1'
			//sh 'rc-service mariadb start'
			//sh '/usr/bin/mysqld_safe &'
			//sh 'sleep 30' // for mysql to startup
			//sh 'mysql -u root -e "CREATE DATABASE HA;"'
			//sh 'mysql -u root HA < WebContent/WEB-INF/log4j.sql'
		    } 
		}
		stage('Build Backend') {
			steps {
				
				withMaven() {
					//properties([pipelineTriggers([snapshotDependencies()])])
					sh '$MVN_CMD -DskipTests=true -T 1C -B clean package'
					//sh 'mvn org.pitest:pitest-maven:mutationCoverage -DtimeoutConstant=8000'
            	}
            }
		}
	
	    stage('Deploy') {
	       parallel {
//	       	    stage('CodeCoverage') {
//	       	    	steps {
//	       		 	   sh 'cd HomeAutomation && bash <(curl -s https://codecov.io/bash)'
//	       			}
//	       		}
	       		//stage('Sonarqube' ) {
	       		//	steps {
	       		//		withMaven() {
	       		//			// org.jacoco:jacoco-maven-plugin:prepare-agent
	       		//    		sh 'MVN_CMD -DskipTests=true sonar:sonar -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_TOKEN -Dsonar.organization=homeautomation'
	       		//		}
	       		//	}
	       		//}

	      		 //stage('JUnit') {
			//		steps {
						//junit '**/target/surefire-reports/**/*.xml'  
		          //  }
			//	}
				stage('Deploy Backend') {
	        		steps {
	   					sh 'ssh root@192.168.1.36 docker-compose stop ha-tomcat'
	        			sh 'rsync -auv --delete --exclude "*.java" target/HomeAutomation-0.0.1-SNAPSHOT/WEB-INF/* root@192.168.1.36:/mnt/raid/tomcat8/webapps/HomeAutomation/WEB-INF/'
	   					sh 'ssh root@192.168.1.36 docker-compose start ha-tomcat'
	   				}
	   			}
//				stage('Archive') {
//	   			    steps {
//	   			        archiveArtifacts artifacts: '**/target/**/*.jar', fingerprint: true
//	   			    	archiveArtifacts artifacts: '**/pom.xml', fingerprint: true
//	   			    }	   			    
//	   			}
	   		}	
	    }
    }
}
