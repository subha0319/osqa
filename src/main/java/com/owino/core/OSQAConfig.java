package com.owino.core;
/*
 * Copyright (C) 2026 Samuel Owino
 *
 * OSQA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OSQA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OSQA.  If not, see <https://www.gnu.org/licenses/>.
 */
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import com.owino.core.OSQAModel.OSQAFeature;
import tools.jackson.databind.ObjectMapper;
import com.owino.core.OSQAModel.OSQATestCase;
import com.owino.core.OSQAModel.OSQATestSpec;
import com.owino.core.OSQAModel.OSQAVerification;
public class OSQAConfig {
    public static final String MODULE_FILE = "data/features.json";
    public static final String MODULE_DIR = "data";
    public static Result<Void> loadFeaturesListFile(){
        try {
            Path envFile = Paths.get(MODULE_DIR + "/" + "env.properties");
            if (Files.notExists(envFile)){
                var dir = Paths.get(MODULE_DIR);
                if (Files.notExists(dir)) Files.createDirectory(dir);
                envFile = Files.createFile(Paths.get("data" + "/" + "env.properties"));
                Files.writeString(envFile,"features-file = " + OSQAConfig.MODULE_FILE);
            }
            return Result.success(null);
        } catch (IOException error) {
            return Result.failure("Failed to load features list file: cause " + error.getLocalizedMessage());
        }
    }
    public static Result<OSQAFeature> loadFeature(String featuresFile) {
        try {
            var json = Files.readString(Paths.get(featuresFile));
            var features = new ObjectMapper().readValue(json, OSQAFeature.class);
            return Result.success(features);
        } catch (IOException error){
            return Result.failure(error.getLocalizedMessage());
        }
    }
    public static Result<OSQATestSpec> loadTestCaseSpec(OSQATestCase testCase) {
        try {
            var specFile = Paths.get(testCase.specFile());
            var json = Files.readString(specFile);
            var testSpec = new ObjectMapper().readValue(json,OSQATestSpec.class);
            return Result.success(testSpec);
        } catch (IOException error){
            error.printStackTrace();
            IO.println("failed to load test case spec from file " + testCase.specFile() + " Cause " + error.getLocalizedMessage());
            return Result.failure(error.getLocalizedMessage());
        }
    }
    public static String timestampedName(LocalDateTime createdTime, String ext) {
        var formater = DateTimeFormatter.ofPattern("yyyy-MM-dd-hh-mm-ss");
        return createdTime.format(formater) + "." + ext;
    }
    public static Result<Void> writeSpecFile(Path appDataDir, OSQATestSpec specification, String specFile) {
        try {
            var nameBuilder = new StringBuilder(appDataDir.toUri().getPath());
            nameBuilder.append("/");
            nameBuilder.append(specFile);
            var path = Paths.get(nameBuilder.toString());
            Files.writeString(path, new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(specification));
            if (Files.exists(path)) return Result.success(null);
            else return Result.failure("Failed to write spec file " + specFile + ": Unknown error");
        } catch (IOException ex){
            return Result.failure("Failed to write test spec file:" +ex.getLocalizedMessage());
        }
    }
    public static Result<Path> writeFeature(Path appDataDir, OSQAFeature feature){
        try {
            var prefix = "feature";
            var nameBuilder = new StringBuilder(appDataDir.toUri().getPath());
            nameBuilder.append("/");
            nameBuilder.append(prefix);
            nameBuilder.append(feature.name().replaceAll(" ",""));
            nameBuilder.append(timestampedName(LocalDateTime.now(),"json"));
            var fileName = nameBuilder.toString();
            var path = Paths.get(fileName);
            Files.writeString(path, new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(feature));
            if (Files.exists(path)) return Result.success(path);
            else return Result.failure("Failed to create features conf file: Error unknown");
        } catch (IOException ex){
            return Result.failure("Failed to write features spec file:" +ex.getLocalizedMessage());
        }
    }
    public static Result<List<OSQAFeature>> listFeatures(Path featuresDir) {
        var folderExists = Files.exists(featuresDir);
        if (!folderExists) return Result.failure("Failed to list features, app dir does not exist");
        try(var dirWalk = Files.walk(featuresDir)){
            List<OSQAFeature> features = dirWalk.sorted(Comparator.reverseOrder())
                    .map(file -> new OSQAModel.OSQAFilesDirTuple(file.getFileName().toString(),file))
                    .filter(tuple -> tuple.fileName().startsWith("feature") && tuple.fileName().endsWith(".json"))
                    .map(tuple -> {
                        try {
                            var rawContents = Files.readString(tuple.absPath());
                            if (rawContents.isBlank()) throw new RuntimeException("Failed to read features, file is empty" + tuple.absPath());
                            IO.println(rawContents);
                            return new ObjectMapper().readValue(rawContents, OSQAFeature.class);
                        } catch (IOException ex){
                            throw new RuntimeException("Failed to read features file contents: " + ex.getLocalizedMessage());
                        }
                    })
                    .toList();
            if (features.isEmpty()) return Result.failure("Failed to list features: empty result");
            else return Result.success(features);
        } catch (IOException err){
            return Result.failure("Failed to read feature list due to IO error: " + err.getLocalizedMessage());
        }
    }
    public static Result<Void> overwriteSpecFile(OSQATestSpec updatedTestSpec,OSQATestCase parentTestCase) {
        var json = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(updatedTestSpec);
        try {
            Files.writeString(Paths.get(parentTestCase.specFile()),json,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            return Result.success(null);
        } catch (IOException error){
            return Result.failure(error.getLocalizedMessage());
        }
    }
    public static Result<Void> updateVerificationStatus(OSQATestSpec testSpec,OSQATestCase parentTestCase, OSQAVerification updatedVerification) {
        var affectedVerification = testSpec.verifications()
                .stream()
                .filter(e -> e.uuid().equals(updatedVerification.uuid()))
                .findFirst();
        if (affectedVerification.isEmpty()) return Result.failure("Failed to update verification status: verification was not registered.");
        List<OSQAVerification> updatedVerifications = new ArrayList<>();
        updatedVerifications.add(updatedVerification);
        var unAffectedVerifications = testSpec.verifications()
                .stream()
                .filter(e -> !e.uuid().equals(updatedVerification.uuid()))
                .toList();
        if (!unAffectedVerifications.isEmpty())
            updatedVerifications.addAll(unAffectedVerifications);
        var updatedTestSpec = new OSQATestSpec(testSpec.uuid(),testSpec.action(),updatedVerifications);
        return overwriteSpecFile(updatedTestSpec,parentTestCase);
    }
}
