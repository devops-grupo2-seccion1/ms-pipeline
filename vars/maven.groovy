import pipeline.*

def call(String chosenStages){

    def utils  = new test.ValidateUtility()

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