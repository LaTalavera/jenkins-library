import groovy.json.JsonSlurper

def call(){
    def jsonSlurper = new JsonSlurper()
    def obj = jsonSlurper.parse(new File("E:/daily_dev/bugDetails.json")) 
    def finalTags = []
    String tags  = obj.fields."System.Tags"
    if (tags != null){
        String[] allTags = tags.split(';')
        for (tag in allTags){
            finalTags.add(tag.trim())
        }
        return finalTags  
    }
    return tags
}

//refactored code IT NEEDS TESTING
// def call() {
//     def jsonSlurper = new JsonSlurper()
//     def obj = jsonSlurper.parse(new File("E:/daily_dev/bugDetails.json")) 
//     def finalTags = []
//     def tags = obj.fields."System.Tags"
//     if (tags) {
//         finalTags = tags.split(';').collect { it.trim() }
//     }
//     return finalTags
// }