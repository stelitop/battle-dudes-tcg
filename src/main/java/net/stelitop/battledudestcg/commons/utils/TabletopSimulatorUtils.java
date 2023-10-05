package net.stelitop.battledudestcg.commons.utils;

import net.stelitop.battledudestcg.game.database.entities.cards.Card;
import net.stelitop.battledudestcg.game.database.entities.cards.DudeCard;
import net.stelitop.battledudestcg.game.database.entities.collection.CardDeck;
import net.stelitop.battledudestcg.game.database.repositories.ChestRepository;
import net.stelitop.battledudestcg.game.database.repositories.DeckRepository;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

@Component
public class TabletopSimulatorUtils {

    @Autowired
    private DeckRepository deckRepository;
    @Autowired
    private ChestRepository chestRepository;

    public Mat createDeckCardSheet(CardDeck deck) throws IOException {
        var cards = deck.getCards();

        int i = 0;
        Mat carddeck = new Mat(7000, 7000, 16, new Scalar(255, 255, 255));
        for (var card : cards) {
            //if (i >= 1) break;
            if (i >= 69) break;
            int col = i % 10;
            int row = i / 10;

            Mat cardMat = createMatOfCard(card);
            Imgcodecs.imwrite("card1.png", cardMat);
            Mat finalSubmat = carddeck.submat(new Rect(700*col, 1000*row, 700, 1000));
            cardMat.copyTo(finalSubmat);
            i++;
        }
        return carddeck;
    }

    public Mat createMatOfCard(Card card) throws IOException {
        URL url = new URL(card.getArtUrl());
        URLConnection connection = url.openConnection();
        Mat finalMat = new Mat(1000, 700, 16, new Scalar(255, 255, 255));

        try (InputStream inputStream = connection.getInputStream()) {
            byte[] imageBytes = inputStream.readAllBytes();
            Mat mat = Imgcodecs.imdecode(new MatOfByte(imageBytes), Imgcodecs.IMREAD_COLOR);
            Imgproc.resize(mat, mat, new Size(600, 600));
            mat.copyTo(finalMat.submat(new Rect(50, 100, 600, 600)));

            // Add text to the image
            //String text = card.getEffectText();
            String text = card.getEffectText().isEmpty() ? "(no effect)" : card.getEffectText();
            List<String> parts = splitTextIntoLines(text);

            Point position = new Point(50, 750); // Position of the text
            Scalar color = new Scalar(0, 0, 0); // Color of the text (BGR format)
            int font = Imgproc.FONT_HERSHEY_COMPLEX; // Font type
            double fontScale = 1; // Font scale
            int thickness = 2; // Thickness of the text
            int lineHeight = 43; // Vertical space between lines

            // Write each line of text on the image
            for (int i = 0; i < parts.size(); i++) {
                Point linePosition = new Point(position.x, position.y + i * lineHeight);
                Imgproc.putText(finalMat, parts.get(i), linePosition, font, fontScale, color, thickness);
            }

            // put the name on the card
            Imgproc.putText(finalMat, card.getName(), new Point(25, 50), font, fontScale*1.5, color, thickness);
            // put the types on the card
            for (int i = 0; i < card.getTypes().size(); i++) {
                Imgproc.putText(finalMat, card.getTypes().get(i).name(),
                        new Point(500, 50 + i*lineHeight), font, fontScale*1.5, color, thickness);
            }
            // put the cost on the card under the name
            String costText = String.valueOf(card.getCost());
            Imgproc.putText(finalMat, costText, new Point(25, 100), font, fontScale*1.5, color, thickness);
            // if it's a dude, put the stats on the card
            if (card instanceof DudeCard dudeCard) {
                String statsText = dudeCard.getAttack() + "/" + dudeCard.getHealth();
                Imgproc.putText(finalMat, statsText, new Point(25, 975), font, fontScale*1.5, color, thickness);
            }

            return finalMat;
        }
    }

    private List<String> splitTextIntoLines(String text) {
        final int maxLength = 32;
        List<String> ret = new ArrayList<>();
        StringBuilder sentence = new StringBuilder();
        for (String word : text.split(" ")) {
            if (sentence.length() + word.length() > maxLength) {
                ret.add(sentence.toString());
                sentence = new StringBuilder();
            }
            sentence.append(word).append(" ");
        }
        ret.add(sentence.toString());
        return ret;
    }
}
