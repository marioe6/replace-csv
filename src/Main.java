import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;


public class Main {
    static final String QUOTATION_MARKS = "\"";
    static final String STARTING_URL = "YOUR_URL";


    public static void main(String[] args) throws Exception {
        File mainFile = new File("main.csv");
        File lookFile = new File("look.csv");

        CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
        try (
                Reader mainReader = Files.newBufferedReader(Paths.get(mainFile.getAbsolutePath()), Charset.forName("UTF-8"));
                Reader lookReader = Files.newBufferedReader(Paths.get(lookFile.getAbsolutePath()), Charset.forName("UTF-8"));
                CSVReader mainCsvReader = new CSVReaderBuilder(mainReader).withCSVParser(parser).build();
                CSVReader lookCsvReader = new CSVReaderBuilder(lookReader).withCSVParser(parser).build();
        ) {

            Map<String, String> lookCsvWords = storeLookCsvInSet(lookCsvReader);

            FileWriter replaced = new FileWriter(createReplacedCsv(0));

            StringBuilder builder = new StringBuilder();
            StringBuilder header = new StringBuilder();
            int line = 0;
            int replacedUrl = 0;
            int notReplacedUrl = 0;
            String[] wordsInLine = null;

            while ((wordsInLine = mainCsvReader.readNext()) != null) {
                try {
                    for (String word : wordsInLine) {

                        if (line == 0) {
                            header.append(QUOTATION_MARKS).append(word).append(QUOTATION_MARKS).append(";");
                        }

                        word = word.replace(QUOTATION_MARKS, "").trim();

                        if (lookCsvWords.containsKey(word) && line != 0) {
                            System.out.println("+ url da sostituire -> " + word +" : "+ lookCsvWords.get(word));
                            word = lookCsvWords.get(word);
                            replacedUrl++;
                        } else if (word.contains(STARTING_URL)) {
                            notReplacedUrl++;
                            System.out.println("- url non trovata nel file look -> " + word);
                        }
                        builder.append(QUOTATION_MARKS).append(word).append(QUOTATION_MARKS).append(";");
                    }
                    line++;
                    builder.deleteCharAt(builder.lastIndexOf(";"));
                    builder.append("\n");

                    if (line % 1000 == 0) {
                        replaced.write(builder.toString());
                        replaced.close();
                        replaced = new FileWriter(createReplacedCsv(line));
                        builder = new StringBuilder();
                        builder.append(header);
                        builder.deleteCharAt(builder.lastIndexOf(";"));
                        builder.append("\n");
                    }
                } catch (Exception e) {
                    System.out.println("Errore lettura main linea " + line);
                    line++;
                }
            }
            replaced.write(builder.toString());
            replaced.close();

            System.out.println("Sostituite " + replacedUrl + " linee su " + line);
            System.out.println("In look ci sono " + lookCsvWords.size() + " linee");
            System.out.println("Url non sostituite " + notReplacedUrl + "");

        }
    }

    private static File createReplacedCsv(int line) throws Exception {
        File replacedCsv = new File("replaced" + line + ".csv");
        replacedCsv.createNewFile();
        return replacedCsv;
    }

    private static Map<String, String> storeLookCsvInSet(CSVReader lookCsvReader) throws Exception {
        Map<String, String> lookCsvWordMappings = new HashMap<String, String>();

        int index = 0;
        String[] wordsInLine = null;
        while ((wordsInLine = lookCsvReader.readNext()) != null) {

            try {
                if (index != 0) {
                    lookCsvWordMappings.put(wordsInLine[0].trim(), wordsInLine[1].trim());
                }
                index++;

            } catch (Exception e) {
                System.out.println("Errore lettura look linea " + index);
                index++;
            }
        }

        return lookCsvWordMappings;
    }

}