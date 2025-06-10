def call(Tests) 
{
    Tests[2] += 1
    slackSend color : "warning", message: "Test ${env.FILE} has timeout \n Please review the report file", channel: '#test_harness_logs_phoenix'
}