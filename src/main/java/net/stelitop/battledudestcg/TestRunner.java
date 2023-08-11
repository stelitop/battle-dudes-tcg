package net.stelitop.battledudestcg;

import net.stelitop.battledudestcg.game.database.repositories.CardRepository;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

//@Component
public class TestRunner implements ApplicationRunner {

    @Autowired
    private CardRepository cardRepository;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        var cards = cardRepository.findAll();

        int i = 0;
        Mat carddeck = new Mat(7000, 7000, 16, new Scalar(255, 255, 255));
        for (var card : cards) {
            //if (i >= 1) break;
            if (i >= 69) break;
            System.out.println("Started: " + card.getName());
            int col = i % 10;
            int row = i / 10;
            URL url = new URL(card.getArtUrl());
            URLConnection connection = url.openConnection();
            try (InputStream inputStream = connection.getInputStream()) {
                byte[] imageBytes = inputStream.readAllBytes();
                Mat mat = Imgcodecs.imdecode(new MatOfByte(imageBytes), Imgcodecs.IMREAD_COLOR);
                //Imgcodecs.imwrite("card1.png", mat);
                Imgproc.resize(mat, mat, new Size(700, 700));
                //Imgcodecs.imwrite("card2.png", mat);
                //mat.copyTo(carddeck.rowRange(700*col, 700*(col+1)).colRange(1000*row, 1000*row + 700));
                Mat finalSubmat = carddeck.submat(new Rect(700*col, 1000*row, 700, 700));
                mat.copyTo(finalSubmat);
            }
            i++;
        }
        Imgcodecs.imwrite("cards.png", carddeck);
    }
}
