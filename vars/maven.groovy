import pipeline.*

def call(){

    def utils  = new test.ValidateUtility()
    stages.each{
        stage('test'){
            utils.isCIorCD()
        }
    }
}