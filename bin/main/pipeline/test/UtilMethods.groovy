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
	} else if(env.GIT_BRANCH.contains('release')){
		figlet 'Entrega Continua'
		return 'RELEASE'
	}else{
		error "Error rama ${env.GIT_BRANCH} no se puede procesar"
	}
}

return this;