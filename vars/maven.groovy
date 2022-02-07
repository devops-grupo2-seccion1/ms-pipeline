import pipeline.*

def call(String chosenStages){
    def utils  = new test.UtilMethods()
    def pipelineStages = (utils.isCIorCD().contains('CI')) ? ['compile','unitTest','jar','sonar','nexusUpload'] : ['gitDiff','nexusDownload','runArtefact','test', 'gitMergeMaster', 'gitMergeDevelop', 'gitTagMaster'] 
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
     if (env.GIT_BRANCH.contains('release')){
        sh "cd ${env.WORKSPACE}"
        sh "git checkout main"
        sh "git pull"
        sh "git merge ${env.GIT_BRANCH}"
        sh "git push origin main"
    }else{
        println "No existe nada que mergear a main de la rama ${env.GIT_BRANCH}"
    }
}

def gitMergeDevelop(){
    if (env.GIT_BRANCH.contains('release')){
        sh "cd ${env.WORKSPACE}"
        sh "git checkout develop"
        sh "git pull"
        sh "git merge ${env.GIT_BRANCH}"
        sh "git push origin develop"
    }else{
        println "No existe nada que mergear a develop de la rama ${env.GIT_BRANCH}"
    }
}

def gitTagMaster(){

}

return this;