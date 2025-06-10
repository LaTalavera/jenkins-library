def call(String destination_ip, String filepath){
    bat"""
        call "C:/Program Files (x86)/Microsoft GDK/Command Prompts/GamingDesktopVars.cmd" GamingDesktopVS2022
        xbconnect ${destination_ip}
        xbapp install ${filepath}
    """
}