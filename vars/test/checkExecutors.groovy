pipeline {
    agent any

    stages {
        stage('Check executors idle') {
            steps {
                script{

                    def executables = Jenkins.instance.computers.collect {c -> c.executors}
                    def runs = Jenkins.instance.computers.collect {c -> c.executors}.flatten().findAll { executor -> executor.isBusy() }.collect { executable -> "${executable.displayName}: ${executable.number}" }
                    //println executables
                    if (runs.isEmpty() == false){
                        println "no hay executors libres"    
                    }else{
                        println "tienes executors libres"
                    }
                }
            }
        }
    }
}
