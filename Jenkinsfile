pipeline {
  agent any

  environment {
    // Diese Werte werden aus Jenkins "Credentials" (Secret Text) geladen
    DB_URL = 'jdbc:postgresql://postgres:5432/franchise'
    DB_USER = 'franchiseuser'
    DB_PASS = credentials('db-pass')
    JWT_SECRET = credentials('jwt-secret')
    RECAPTCHA_SECRET = credentials('recaptcha-secret')
    RECAPTCHA_SITE_KEY = credentials('recaptcha-site-key')

    // Wird ins Frontend geschrieben
    API_URL = 'http://localhost:9002'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Generate .env files') {
      steps {
        sh '''
          echo "Generiere backend/.env ..."
          cat > backend/.env <<EOF
          DB_URL=$DB_URL
          DB_USER=$DB_USER
          DB_PASS=$DB_PASS
          JWT_SECRET=$JWT_SECRET
          RECAPTCHA_SECRET=$RECAPTCHA_SECRET
          spring.profiles.active=prod
          EOF

          echo "Generiere frontend/.env.production ..."
          cat > frontend/.env.production <<EOF
          REACT_APP_RECAPTCHA_SITE_KEY=$RECAPTCHA_SITE_KEY
          REACT_APP_API_URL=$API_URL
          EOF
        '''
      }
    }

    stage('Build Docker Images') {
      steps {
        sh 'docker-compose -f docker-compose.prod.yml build'
      }
    }

    stage('Stop old containers') {
      steps {
        sh 'docker-compose -f docker-compose.prod.yml down'
      }
    }

    stage('Start new containers') {
      steps {
        sh 'docker-compose -f docker-compose.prod.yml up -d'
      }
    }
  }
}
