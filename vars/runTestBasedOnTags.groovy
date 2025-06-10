def call(){
    def tagList = env.TAGS.split(',').collect{it as String}
    for(tag in tagList){
        tag = tag.replaceAll("\\[|\\]", "").trim()
        println tag
        switch(tag) {            
            case "Pathfinding": 
                slackSend color : "warning", message: "Pathfinding tag detected in the ticket associated to the commit, running pathfinding pipeline", channel: '#jenkins_bot_phoenix'
                build job: 'pathfinding', propagate: true, wait: true
                break; 
            case "Gameplay": 
                slackSend color : "warning", message: "Gameplay tag detected in the ticket associated to the commit, running Gameplay pipeline", channel: '#jenkins_bot_phoenix'
                build job: 'gameplay', propagate: true, wait: true
                break; 
            case "CommunityFeedback": 
                build job: 'communityFeedback', propagate: true, wait: true
                break; 
            case "Performance": 
                build job: 'performance', propagate: true, wait: true
                break; 
            default: 
                println("No test pipeline developed for that tag"); 
                break; 
        }
    }
}
