pipeline {
    agent {
        docker {
            image 'maven:3.5.4-jdk-8-alpine' 
            args '-v /root/.ssh:/root/.ssh -v $HOME/.m2:/root/.m2' 
        }
    }
    triggers { upstream(upstreamProjects: 'comdata/HomeAutomationBase,comdata/olingo-jpa-processor-v4', threshold: hudson.model.Result.SUCCESS)}
    
    stages {
		stage('Prepare') {
		    steps {
			sh 'apk update'
			sh 'apk add nodejs rsync graphviz openssh mariadb mariadb-client openrc git'
			sh 'mysql_install_db --user=mysql --rpm'
			sh '/usr/bin/mysqld_safe &'
			sh 'sleep 5' // for mysql to startup
			sh 'mysql -u root -e "CREATE DATABASE HA;"'
			sh 'mysql -u root HA < WebContent/WEB-INF/log4j.sql'
		    } 
		}
		stage('Build Backend') {
			steps {
				
				withMaven() {
					//properties([pipelineTriggers([snapshotDependencies()])])
					sh 'mvn -T 1C -B clean deploy'
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
	       				withMaven() {
	       					// org.jacoco:jacoco-maven-plugin:prepare-agent
	       		    		sh 'mvn -DskipTests=true sonar:sonar -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=$SONAR_TOKEN -Dsonar.organization=homeautomation'
	       				}
	       			}
	       		}

	      		 //stage('JUnit') {
			//		steps {
						//junit '**/target/surefire-reports/**/*.xml'  
		          //  }
			//	}
				stage('Deploy Backend') {
	        		steps {
	        			sh 'rsync -auv --delete --exclude "*.java" target/HomeAutomation-0.0.1-SNAPSHOT/WEB-INF/* root@192.168.1.76:/var/lib/tomcat8/webapps/HomeAutomation/WEB-INF/'
	   					sh 'ssh root@192.168.1.76 /etc/init.d/tomcat8 restart'
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
