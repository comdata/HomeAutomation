pipeline {
    agent {
        docker {
            image 'maven:3.5.4-jdk-8-alpine' 
            args '-v /root/.ssh:/root/.ssh' 
        }
    }
    triggers { upstream(upstreamProjects: 'comdata/HomeAutomationBase', threshold: hudson.model.Result.SUCCESS)}
    
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
		stage('Build-Parent') { 
			steps {
			
					sh 'mvn -T 1C -N install'
			}
		}
		stage('Build Backend') {
			steps {
				properties([pipelineTriggers([snapshotDependencies()])])
				withMaven() {
					sh 'cd HomeAutomation && mvn -T 1C -B clean deploy'
            	}
            }
		}
	
	    stage('Deploy') {
	       parallel {
	       	    //stage('CodeCoverage') {
	       	    //	steps {
	       		// 	   sh 'cd HomeAutomation && bash <(curl -s https://codecov.io/bash)'
	       	//		}
	       	//	}
	       		stage('Sonarqube') {
	       			steps {
	       		    	sh 'cd HomeAutomation && mvn -DskipTests=true org.jacoco:jacoco-maven-plugin:prepare-agent sonar:sonar -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_TOKEN -Dsonar.organization=homeautomation'
	       			}
	       		}

	      		 //stage('JUnit') {
			//		steps {
						//junit '**/target/surefire-reports/**/*.xml'  
		          //  }
			//	}
				stage('Deploy Backend') {
	        		steps {
	        			sh 'rsync -auv --delete --exclude "*.java" HomeAutomation/target/HomeAutomation-0.0.1-SNAPSHOT/WEB-INF/* root@192.168.1.76:/var/lib/tomcat8/webapps/HomeAutomation/WEB-INF/'
	   					sh 'ssh root@192.168.1.76 /etc/init.d/tomcat8 restart'
	   				}
	   			}
				stage('Archive') {
	   			    steps {
	   			        archiveArtifacts artifacts: '**/target/**/*.jar', fingerprint: true
	   			    	archiveArtifacts artifacts: '**/pom.xml', fingerprint: true
	   			    }

	   			    
	   			}
	   		}	
	    }
    }
}
