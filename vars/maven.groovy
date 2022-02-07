import pipeline.*

def call(String chosenStages){
    def utils  = new test.UtilMethods()
    def pipelineStages = (utils.isCIorCD().contains('CI')) ? [/* 'compile','unitTest','jar','sonar','nexusUpload', */'gitDiff'] : ['gitDiff','nexusDownload','runArtefact','test', 'gitMergeMaster', 'gitMergeDevelop', 'gitTagMaster'] 
    def stages = utils.getValidatedStages(chosenStages, pipelineStages)

    env.PIPELINE_INTEGRATIONS = utils.isCIorCD();
    env.PIPELINE_STAGES = pipelineStages.join(' ');
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
    def git = new git.GitMethods()
    if (env.GIT_BRANCH.contains('develop')){
        
    }else if(env.GIT_BRANCH.contains('feature')){
        
    }else{
        println "La rama ${env.GIT_BRANCH} no corresponde como rama de orígen para la creación de un Release."
    }
}

// funciones para CD
def gitDiff(){
    echo 'Diferencias con branch main'
    sh "git diff --name-only origin/main..${env.GIT_BRANCH}"
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