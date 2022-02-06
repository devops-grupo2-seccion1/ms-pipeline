def call(){
    pipeline {
        agent any
        stages {
            stage("Pipeline"){
                figlet 'Iniciando ejecución Pipeline'
            }
        }
    }
}

return this;