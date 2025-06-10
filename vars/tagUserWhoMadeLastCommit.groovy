def call(String p4_user ){
    def userId = "None"
    def userIds = sharedVariables.getUserIds()
    userIds.each { user, id ->        
        if (user == p4_user){
            userId = id
        }
    }
    return userId
}