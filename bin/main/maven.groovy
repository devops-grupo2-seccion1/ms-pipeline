import pipeline.*

def call(String chosenStages){
    def utils  = new test.UtilMethods()
    def pipelineStages = (utils.isCIorCD().contains('CI')) ? ['compile','unitTest','jar','sonar','nexusUpload','gitCreateRelease'] : ['gitDiff','nexusDownload','runArtefact','test', 'gitMergeMaster', 'gitMergeDevelop', 'gitTagMaster'] 
    def stages = utils.getValidatedStages(chosenStages, pipelineStages)

    env.PIPELINE_INTEGRATIONS = utils.isCIorCD();
    env.PIPELINE_STAGES = pipelineStages.join(' ');
    stages.each{
        stage(it){
            try {
                figlet "Stage ${it}"
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
    def src = GIT_BRANCH.split("\\/")
    def folder = src[0]
    def rama = src[1]
    def build = BUILD_NUMBER
    def repox =GIT_URL.split("\\/")
    def repo = repox[repox.length-1].replace(".git","")
    def nombre="${repo}-${rama}-${build}"
    def pkey="${repo}-${rama}"
    withSonarQubeEnv('sonarqube') {
        sh "mvn clean verify sonar:sonar -Dsonar.projectKey=${pkey} -Dsonar.projectName=${nombre}"
    }
}

def nexusUpload(){
        pom = readMavenPom(file: 'pom.xml')
        def src = GIT_BRANCH.split("\\/")
        def folder = src[0]
        def rama = src[1]
        nexusPublisher nexusInstanceId: 'nexus',
        nexusRepositoryId: 'devops-usach-nexus',  // REPOSITORIO!!!!!
        packages: [
            [$class: 'MavenPackage',
                mavenAssetList: [
                    [classifier: '',
                    extension: 'jar',
                    filePath: "build/"+pom.artifactId+"-"+pom.version+".jar"
                ]
            ],
                mavenCoordinate: [
                    artifactId: pom.artifactId+"-"+rama,
                    groupId: pom.groupId,
                    packaging: 'jar',
                    version: pom.version
                ]
            ]
        ]
}

def gitCreateRelease(){
    def git = new git.GitMethods()
    if (env.GIT_BRANCH.contains('develop')){
        pom = readMavenPom(file: 'pom.xml')
        env.VERSION_EXCUTE = (pom.version).replace(".", "-")
        if (git.checkIfBranchExists('release-v' + env.VERSION_EXCUTE)){
            println 'La rama existe'
            git.deleteBranch('release-v' + env.VERSION_EXCUTE)
            println 'Rama eliminada. Se crea nuevamente.'
            git.createBranch('develop','release-v' + env.VERSION_EXCUTE, env.VERSION_EXCUTE)
            println 'Rama creada con ??xito.'
        } else {
            git.createBranch('develop','release-v' + env.VERSION_EXCUTE, env.VERSION_EXCUTE)
            println 'Rama creada con ??xito.'
        }
    }else{
        println "La rama ${env.GIT_BRANCH} no corresponde como rama de or??gen para la creaci??n de un Release."
    }
}

// funciones para CD
def gitDiff(){
    echo 'Diferencias con branch main'
    sh "git diff --name-only origin/main..${env.GIT_BRANCH}"
}

def nexusDownload(){
    pom = readMavenPom(file: 'pom.xml')
    def src = GIT_BRANCH.split("\\/")
    def folder = src[0]
    def rama = src[1]
    def carpeta=(pom.artifactId).toLowerCase()
    println("${folder}-|-${rama}-|-${pom.artifactId}-|-${carpeta}")
    def urlx = "http://nexus:8081/repository/devops-usach-nexus/com/${carpeta}/${pom.artifactId}-${rama}/${pom.version}/${pom.artifactId}-${rama}-${pom.version}.jar"
    println(urlx)
    sh " curl -X GET '${urlx}'  -O"
}

def runArtefact(){
    pom = readMavenPom(file: 'pom.xml')
    def src = GIT_BRANCH.split("\\/")
    def folder = src[0]
    def rama = src[1]
    sh "nohup java -jar ${pom.artifactId}-${rama}-${pom.version}.jar & >/dev/null"
    sleep 20
}

def test(){
    sh "curl -X GET http://localhost:8080/rest/mscovid/test?msg=testing"
    sh "curl -X GET http://localhost:8080/rest/mscovid/estadoMundial?msg=testing"
}

def gitMergeMaster(){
     if (env.GIT_BRANCH.contains('release')){
        sh "cd ${env.WORKSPACE}"
        sh "git checkout main"
        sh "git pull origin main"
        sh "git merge ${env.GIT_BRANCH}"
        sh "git push -f origin main"
    }else{
        println "No existe nada que mergear a main de la rama ${env.GIT_BRANCH}"
    }
}

def gitMergeDevelop(){
    if (env.GIT_BRANCH.contains('release')){
        sh "cd ${env.WORKSPACE}"
        sh "git checkout develop"
        sh "git pull origin develop"
        sh "git merge ${env.GIT_BRANCH}"
        sh "git push -f origin develop"
    }else{
        println "No existe nada que mergear a develop de la rama ${env.GIT_BRANCH}"
    }
}

def gitTagMaster(){
    pom = readMavenPom(file: 'pom.xml')
    def version = 'v' + pom.version
    sh "git checkout main"
    sh "git tag -fa ${version} -m 'add tag'"
    sh "git push origin --tags"
}

return this;