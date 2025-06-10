def call(){
    def msg = findItemInChangelog("msg")
    def bugNumber = msg.find( /\d{6,7}/ ).toString()
    if (bugNumber.size() == 6 || bugNumber.size() == 7){
        writeFile file: 'bugNumber.txt', text: "${bugNumber}" 
    }     
} 
