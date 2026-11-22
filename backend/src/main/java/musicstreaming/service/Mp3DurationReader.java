package musicstreaming.service;

import com.mpatric.mp3agic.Mp3File;
import org.springframework.stereotype.Component;
import java.io.File;

@Component
public class Mp3DurationReader {
    public int readDurationSeconds(File file) {
        try {
            Mp3File mp3file = new Mp3File(file);
            return (int) mp3file.getLengthInSeconds();
        } catch (Exception e) {
            throw new RuntimeException("Cannot read MP3 duration: " + e.getMessage());
        }
    }
}