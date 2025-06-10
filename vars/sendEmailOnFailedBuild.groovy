def call(){
    def emails = [
        "charlieh": "charbord@forgottenempires.net",
        "e-sobra": "sobraztsov@forgottenempires.net",
        "ivank": "ikirichenko@forgottenempires.net",
        "jcarr": "jcarr@forgottenempires.net",
        "Marius Beck": "mbeck@forgottenempires.net",
        "raymik": "rklingers@forgottenempires.net",
        "tapsa": "mpartonen@forgottenempires.net",
        "cgourdie":  "cgourdie@forgottenempires.net",
        "aoe_scout": "emarinov@forgottenempires.net",
        "mwinocur": "mwinocur@forgottenempires.net",
        "kwright": "kwright@forgottenempires.net",
        "mwinocur" : "mwinocur@forgottenempires.net",
        "kwright" : "kwright@forgottenempires.net",
        "jfernandez": "jfernandez@forgottenempires.net"
    ]
    def email = "jfernandez@forgottenempires.net"
    def receiver = findItemInChangelog("changeUser")
                        
    for (key in emails.keySet()) {
        if (receiver == key){
            email = emails.get(key)
        }
    }
    mail to: email,
    subject: "Status of: ${currentBuild.fullDisplayName}",
    body: "Phoenix has failed to build on FE Build machine from your last commit CL#'${env.P4_CHANGELIST} please review your last commit"    
}
