def call(){

    pipeline {
        agent any

        parameters { 
            choice(name: 'buildtool', choices: ['maven','gradle'], description: 'Elección de herramienta de construcción para aplicación covid')
            string(name: 'stages', defaultValue: '' , description: 'Escribir stages a ejecutar en formato: stage1;stage2;stage3. Si stage es vacío, se ejecutarán todos los stages.')
        }

        stages {
            stage('Pipeline') {
                steps {
                    script{

                        sh 'env'

                        figlet params.buildtool
                        def archivo = (params.buildtool == 'gradle') ? 'build.gradle' : 'pom.xml'

                        if (fileExists(archivo)){
                            "${params.buildtool}" "${params.stages}"
                        } else {
                            error "archivo ${archivo} no existe. No se puede construir pipeline basado en ${params.buildtool}"
                        }

                    }
                }
            }
        }
    }
}

return this;
