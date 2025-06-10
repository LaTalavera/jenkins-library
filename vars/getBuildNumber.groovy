
@NonCPS
def call(String filePath) {
    def versionNumber = null
    def file = new File(filePath)
        
    file.eachLine { line ->
        if (line.startsWith('Version')) {
            def pattern = /\((\d+)\)/
            def matcher = line =~ pattern
            
            if (matcher.find()) {
                versionNumber = matcher[0][1]
                return
            } else {
                println "Number not found in the Version line."
            }
        }
    }        
    return versionNumber
}