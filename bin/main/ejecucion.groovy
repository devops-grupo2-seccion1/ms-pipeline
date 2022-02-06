def call(){
    pipeline {
        agent any
        environment {
            NEXUS_USER         = credentials('NEXUS-USER')
            NEXUS_PASSWORD     = credentials('NEXUS-PASS')
        }
        stages {
            stage("Pipeline"){
                steps {
                    script{
                         maven.call()
                    }
                }
            }
        }
    }
}

return this;