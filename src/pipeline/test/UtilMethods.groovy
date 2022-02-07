package pipeline.test

def getValidatedStages(String chosenStages, ArrayList pipelineStages){

	def stages = []

	if (chosenStages?.trim()){
		chosenStages.split(';').each{
			if (it in pipelineStages){
				stages.add(it)
			} else {
				error "${it} no existe como Stage. Stages disponibles para ejecutar: ${pipelineStages}"
			}
		}
		println "Validación de stages correcta. Se ejecutarán los siguientes stages en orden: ${stages}"
	} else {
		stages = pipelineStages
		println "Parámetro de stages vacío. Se ejecutarán todos los stages en el siguiente orden: ${stages}"
	}
	return stages
}

def isCIorCD(){
	if (env.GIT_BRANCH.contains('develop') || env.GIT_BRANCH.contains('feature')){
		figlet 'Integracion Continua'
		return 'CI'
	} else {
		figlet 'Entrega Continua'
		return 'CD'
	}
}

def tryUrl(){
        def continuar=true
        def intento=0
        def intentoMax=5
        while(continuar){
            intento++
            try {
                sh "curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'  >> /dev/null "
                continuar=false
                sh "echo '#### ARRANCADO Intento:${intento}'"
            } catch (Exception e){
                sh "echo '#### AUN NO ARRANCA intento:${intento}'"
                if(intento>intentoMax){
                    continuar=false
                    sh "echo '#### Fail intento:${intento}'"
                    throw new Exception("Se demoro mucho en arrancar")
                } else {
                    sh "sleep 5"
                }
            }
}

return this;