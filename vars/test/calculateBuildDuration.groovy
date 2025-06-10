@NonCPS

def call(start, end) {
    // Calculate elapsed time
    long elapsedTime = end - start

    // Convert to time units
    Long second = (elapsedTime / 1000).longValue() % 60;
    Long minute = (elapsedTime / (1000 * 60)).longValue() % 60;
    Long hour = (elapsedTime / (1000 * 60 * 60)).longValue() % 24;
    Long remainderMillis = elapsedTime % 1000

    // Return formatted time
    return "${hour}h  ${minute}m ${second}s ${remainderMillis}ms"
}