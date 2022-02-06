import pipeline.*

def call(){
    pipeline {
        agent any
        environment {
            NEXUS_USER         = credentials('NEXUS-USER')
            NEXUS_PASSWORD     = credentials('NEXUS-PASS')
        }
        def utils  = new test.ValidateUtility()
        stages {
            stage("Pipeline"){
                steps {
                    script{
                        
                        utils.isCIorCD()
                    }
                }
            }
        }
    }
}

return this;