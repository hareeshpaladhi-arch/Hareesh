package com.example.NewProject.DataLoader;

import com.example.NewProject.Entity.DictModel;
import com.example.NewProject.Repository.DictRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private DictRepo DictRepo;

    @Override
    public void run(String... args) throws Exception {
        InputStream inputStream =
                Thread.currentThread()
                        .getContextClassLoader()
                        .getResourceAsStream("DictFile.csv");
        if (inputStream == null) {
            throw new RuntimeException("data.csv not found in src/main/resources");
        }

        BufferedReader br = new BufferedReader(
                new InputStreamReader(inputStream));

        br.readLine(); // skip header

        String line;

        while ((line = br.readLine()) != null)
        {

            String[] data = line.split(";");

            String noun = data[0];
            String modifier = data[1];
            String nounmodifier = data[2];

            // ✅ prevent duplicates
            if (DictRepo.findByNounmodifier(nounmodifier).isEmpty()) {

                DictModel dict = new DictModel();
                dict.setNoun(noun);
                dict.setModifier(modifier);
                dict.setNounmodifier(nounmodifier);

                DictRepo.save(dict);
            }
        }
    }
}
