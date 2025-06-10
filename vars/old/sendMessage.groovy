def call(def lastlines, def success)
{
    lastlines = lastlines.reverse()
    def msg = lastlines.join('\n')
    if (success == "true")
        slackSend color : "good", message: "${msg}", channel: '#test_harness_logs_phoenix'
    else
        slackSend color : "danger", message: "${msg}", channel: '#test_harness_logs_phoenix'
}
