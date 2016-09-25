package org.appling.ustoword;

import org.docx4j.Docx4J;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.AltChunkType;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;

import java.io.*;

/**
 * Created by sappling on 9/24/2016.
 */
public class Doctest {
    public static void main(String argv[]) {
        try {
            //WordprocessingMLPackage wordMLPackage = Docx4J.load(new File("styles.docx"));
            InputStream stream = Doctest.class.getResourceAsStream("/styles.docx");
            WordprocessingMLPackage wordMLPackage = Docx4J.load(stream);
            MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
            //documentPart.addParagraphOfText("This is a test");
            //String h1="<html><body><h1>This is heading 1</h1><p>This is under heading 1</p></body></html>";
            String testData = readFile("testdata.html");
            documentPart.addAltChunk(AltChunkType.Html, testData.getBytes());
            wordMLPackage.save(new File("output.docx"));
        } catch (Docx4JException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String readFile(String file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String         line = null;
        StringBuilder  stringBuilder = new StringBuilder();
        String         ls = System.getProperty("line.separator");

        try {
            while((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            return stringBuilder.toString();
        } finally {
            reader.close();
        }
    }
}

