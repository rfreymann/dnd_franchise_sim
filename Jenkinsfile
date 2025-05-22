pipeline {
  agent any
  stages {
    stage('Build Backend') {
      steps {
        dir('backend') {
          sh './mvnw clean package -DskipTests'
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
}
