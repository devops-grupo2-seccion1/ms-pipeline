import pipeline.*

def call(String chosenStages){
    env.PIPELINE_INTEGRATIONS = utils.isCIorCD();
    def utils  = new test.UtilMethods()
    def pipelineStages = (utils.isCIorCD().contains('CI')) ? ['compile','unitTest','jar','sonar','nexusUpload','gitCreateRelease'] : ['gitDiff','nexusDownload','runArtefact','test', 'gitMergeMaster', 'gitMergeDevelop', 'gitTagMaster'] 
    def stages = utils.getValidatedStages(chosenStages, pipelineStages)

    stages.each{
        stage(it){
            try {
                "${it}"()
            }
            catch(Exception e) {
                error "Stage ${it} tiene problemas: ${e}"
            }
        }
    }
}

// funciones para CI
def compile(){
    sh './mvnw clean compile -e'
}

def unitTest(){
    sh './mvnw clean test -e'
}

def jar(){
    sh './mvnw clean package -e'
}

def sonar(){
    withSonarQubeEnv('sonarqube') {
        sh 'mvnw clean verify sonar:sonar -Dsonar.projectKey=ms-iclab-${env.GIT_BRANCH} -Dsonar.java.binaries=build'
    }
}

def nexusUpload(){
    nexusPublisher nexusInstanceId: 'nexus', nexusRepositoryId: 'test-nexus', packages: [[$class: 'MavenPackage', mavenAssetList: [[classifier: '', extension: 'jar', filePath: "build/DevOpsUsach2020-0.0.1.jar"]], mavenCoordinate: [artifactId: 'DevOpsUsach2020', groupId: 'com.devopsusach2020', packaging: 'jar', version: "0.0.1-${env.GIT_BRANCH}"]]]  
}

def gitCreateRelease(){
    if (env.GIT_BRANCH.contains('develop')){
        def git = new git.GitMethods()
        if (git.checkIfBranchExists('release-v1-0-0')){
            println 'La rama existe'
            git.deleteBranch('release-v1-0-0')
            println 'Rama eliminada. Se crea nuevamente.'
            git.createBranch(env.GIT_BRANCH,'release-v1-0-0')
            println 'Rama creada con éxito.'
        } else {
            git.createBranch(env.GIT_BRANCH,'release-v1-0-0')
            println 'Rama creada con éxito.'
        }

    } else {
        println "La rama ${env.GIT_BRANCH} no corresponde como rama de orígen para la creación de un Release."
    }
}

// funciones para CD
def gitDiff(){

}

def nexusDownload(){
    sh ' curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD "http://nexus:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar" -O'
}

def runArtefact(){
    sh 'nohup bash java -jar DevOpsUsach2020-0.0.1.jar & >/dev/null'
    sleep 20
}

def test(){
    sh "curl -X GET http://localhost:8080/rest/mscovid/test?msg=testing"
}

def gitMergeMaster(){

}

def gitMergeDevelop(){

}

def gitTagMaster(){

}

return this;