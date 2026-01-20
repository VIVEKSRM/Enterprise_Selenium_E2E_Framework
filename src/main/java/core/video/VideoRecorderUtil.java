package core.video;

/**
 * Local video recording utility (FFmpeg hook)
 */
public class VideoRecorderUtil {

    private static Process process;

    public static void start(String testName) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-f", "gdigrab",
                "-framerate", "15",
                "-i", "desktop",
                "reports/video_" + testName + ".mp4"
            );
            process = pb.start();
        } catch (Exception e) {
            System.err.println("Video recording start failed");
        }
    }

    public static void stop() {
        if (process != null) {
            process.destroy();
        }
    }
}
