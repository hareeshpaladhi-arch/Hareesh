package com.example.NewProject.DataLoader;

import com.example.NewProject.Entity.DataExemption;
import com.example.NewProject.Entity.DictModel;
import com.example.NewProject.Repository.DataExemptionRepo;
import com.example.NewProject.Repository.DictRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private DictRepo DictRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataExemptionRepo dataExemptionRepo;

    @Override
    public void run(String... args) throws Exception {
        // Delete all existing records
        DictRepo.deleteAll();
        dataExemptionRepo.deleteAll();
        //drop existing table
        //jdbcTemplate.execute("DROP TABLE IF EXISTS Dictionary");

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
            //if (DictRepo.findByNounmodifier(nounmodifier).isEmpty()) {

                DictModel dict = new DictModel();
                dict.setNoun(noun);
                dict.setModifier(modifier);
                dict.setNounmodifier(nounmodifier);

                DictRepo.save(dict);
           // }
        }

        InputStream inputStream1 =
                Thread.currentThread()
                        .getContextClassLoader()
                        .getResourceAsStream("DataExemption.csv");

        if (inputStream1 == null) {
            throw new RuntimeException("DataExemption.csv not found in src/main/resources");
        }

        BufferedReader br1 = new BufferedReader(new InputStreamReader(inputStream1));

        br1.readLine(); // skip header

        String line1;

        while ((line1 = br1.readLine()) != null) {

            String[] data1 = line1.split(";");

            String noun = data1[0];
            String exemption = data1[1];
            DataExemption ex = new DataExemption();
            ex.setNouns(data1[0]);
            ex.setExemption(data1[1]);

            dataExemptionRepo.save(ex);


        }
    }

}
