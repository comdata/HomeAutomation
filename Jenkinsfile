pipeline {
    agent {
        docker {
            image 'comdata456/alpine-mariadb-docker' 
            args '-v $HOME/.m2:/root/.m2 -v /root/.ssh:/root/.ssh' 
        }
    }
    triggers { upstream(upstreamProjects: 'comdata/HomeAutomationBase,comdata/olingo-jpa-processor-v4', threshold: hudson.model.Result.SUCCESS)}
    
    stages {
		stage('Build Backend') {
			steps {
				
				withMaven() {
					sh '$MVN_CMD -DskipTests -B package'
            	}
            }
		}
		
		 stage('Make Container') {

            steps {
                sh "docker build -t comdata456/homeautomation:${env.BUILD_ID} ."
                sh "docker tag comdata456/homeautomation:${env.BUILD_ID} comdata456/homeautomation:latest"
            }
        }
	   
    }
    post {
        always {
            archiveArtifacts artifacts: 'target/**/*.jar', fingerprint: true
        }
        success {
            withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                sh "/usr/bin/docker login -u ${USERNAME} -p ${PASSWORD}"
                sh "/usr/bin/docker push comdata456/homeautomation:${env.BUILD_ID}"
                sh "/usr/bin/docker push comdata456/homeautomation"
            }
        }
    }    
}
