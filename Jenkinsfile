pipeline {
  agent any

  stages {
    stage('Build Backend') {
      steps {
        dir('backend') {
          sh 'chmod +x mvnw'
          sh './mvnw clean package -DskipTests'
        }
      }
    }

    stage('Test Backend') {
      steps {
        dir('backend') {
          sh './mvnw test'
        }
      }
    }

    stage('Build Frontend') {
      steps {
        dir('frontend/franchise-frontend') {
          sh 'npm install'
          sh 'npm run build'
        }
      }
    }
  }

  post {
    always {
      junit 'backend/target/surefire-reports/*.xml'
    }
  }
}
