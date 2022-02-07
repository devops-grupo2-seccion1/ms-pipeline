package pipeline.git

def checkIfBranchExists(String branch){
	def output = sh (script: "git ls-remote --heads origin ${branch}", returnStdout: true)
	if (output?.trim()) {
		return true
	} else {
		return false
	}
}

def deleteBranch(String branch){
	sh "git push origin --delete ${branch}"
}

def createBranch(String origin, String newBranch, String tag){
	sh "cd ${env.WORKSPACE}"
	sh "git fetch -p"
	sh "git branch -d ${newBranch}"
	sh "git checkout ${origin}"
	sh "git pull origin ${origin}"

	sh "git tag v1.0.1"
	sh "git push origin v1.0.0"
/* 	sh "git checkout -b ${newBranch}"
	sh "git push origin ${newBranch}" */
}

def createPullRequest(String origin, String branch){
	sh '''
		git fetch -p 
		git checkout '''+branch+'''; git pull
		git request-pull  '''+branch+'''
	'''
}

return this;