def call(String solutionPath, String configuration, String platform ,String additionalOptions, int maxAttempts = 2) {
    def msbuild = tool name: 'MSBuild', type: 'hudson.plugins.msbuild.MsBuildInstallation'
    String buildOutcome = ""

    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
        try {
            if (attempt == 1) {
                echo "============================"
                echo "Attempt #${attempt}: BUILD"
                echo "============================"
                // Perform the normal build
                bat "\"${msbuild}\" ${solutionPath} /t:Build /p:configuration=\"${configuration}\" /p:Platform=\"${platform}\" /p:AdditionalOptions=\"${additionalOptions}\""
                buildOutcome = "Build (first attempt)"
            } else {
                echo "============================"
                echo "Attempt #${attempt}: REBUILD"
                echo "============================"
                // Perform the rebuild
                bat "\"${msbuild}\" ${solutionPath} /t:Rebuild /p:configuration=\"${configuration}\" /p:Platform=\"${platform}\" /p:AdditionalOptions=\"${additionalOptions}\""
                buildOutcome = "Rebuild (second attempt)"
            }

            // If build or rebuild succeeds, exit the loop
            echo "SUCCESS on attempt #${attempt}."
            break

        } catch (Exception e) {
            echo "FAILURE on attempt #${attempt}."
            if (attempt == maxAttempts) {
                // Re-throw the exception to mark the stage as failed
                throw e
            } else {
                echo "Preparing to retry by doing a rebuild..."
            }
        }
    }
    return buildOutcome
}