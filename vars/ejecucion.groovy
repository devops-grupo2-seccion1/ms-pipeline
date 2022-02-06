def call(){
    pipeline {
        agent any
        stages {
            stage("Pipeline"){
                figlet 'Ejecución pipeline'
            }
        }
    }
}
return this;