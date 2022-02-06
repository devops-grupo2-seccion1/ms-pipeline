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
                        //def ci_or_cd = verifyBranchName()
                        figlet env.GIT_BRANCH
                    }
                }
            }
        }
    }
}

return this;