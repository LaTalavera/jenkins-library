def call() {
    // 1. Collect all the executors from all the computers
    def executables = Jenkins.instance.computers.collect {
        c -> c.executors
    }
    // 2. Flatten the list of executors
    def runs = Jenkins.instance.computers.collect {
        c -> c.executors
    }.flatten().findAll {
        executor -> executor.isBusy()
    }.collect {
        executable -> "${executable.displayName}: ${executable.number}"
    }
    // 3. If there are no executors in the list, there are no busy executors
    if (runs.isEmpty() == false) {
        println "no hay executors libres"
    } else  {
        println "tienes executors libres"
    }
}

// refactored version   
// // 1. Collect all the busy executors from all the computers
// def busyExecutors = Jenkins.instance.computers.collectMany {
//     computer -> computer.executors.findAll {
//         executor -> executor.isBusy()
//     }
// }

// // 2. If there are no busy executors in the list, there are no busy executors
// if (busyExecutors.isEmpty()) {
//     println "You have no busy executors"
// } else {
//     println "You have ${busyExecutors.size()} busy executors:"
//     busyExecutors.each {
//         executor -> println "${executor.displayName}: ${executor.number}"
//     }
// }