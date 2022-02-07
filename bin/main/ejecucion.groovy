def call(){

    pipeline {
        agent any
        environment {
            NEXUS_USER         = credentials('NEXUS-USER')
            NEXUS_PASSWORD     = credentials('NEXUS-PASS')
        }
        
        parameters { 
            string(name: 'stages', defaultValue: '' , description: 'Escribir stages a ejecutar en formato: stage1;stage2;stage3. Si stage es vacío, se ejecutarán todos los stages.')
        }

        stages {
            stage('Pipeline') {
                steps {
                    script{
                        sh 'env'
                        def buildtool = 'maven'
                        if (fileExists('pom.xml')){
                            "${buildtool}" "${params.stages}"
                        }else{
                            error "archivo ${archivo} no existe. No se puede construir pipeline basado en ${params.buildtool}"
                        }
                    }
                }
                post{
                    success{
                        slackSend color: 'good', message: "[Grupo2][Pipeline ${env.PIPELINE_INTEGRATIONS}][Rama: ${env.GIT_BRANCH}][Stage: ${env.PIPELINE_STAGES}][Resultado: OK]", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'token-jenkins-slack'
                    }
                    failure{
                        slackSend color: 'danger', message: "[Grupo2][Pipeline ${env.PIPELINE_INTEGRATIONS}][Rama: ${env.GIT_BRANCH}][Stage: ${env.PIPELINE_STAGES}][Resultado: NO OK]", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'token-jenkins-slack'
                    }
                }
            }
        }
    }
}

return this;
