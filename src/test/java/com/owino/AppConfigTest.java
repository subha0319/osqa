package com.owino;
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
import com.owino.core.OSQAConfig;
import com.owino.core.Result;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import com.owino.core.OSQAModel.OSQAFeature;
import com.owino.core.OSQAModel.OSQATestCase;
import com.owino.core.OSQAModel.OSQATestSpec;
import com.owino.core.OSQAModel.OSQAVerification;
import tools.jackson.databind.exc.ValueInstantiationException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
public class AppConfigTest {
    private final String TEST_CASE_SPEC_FILE = "data/test-001-spec.json";
    private Path featuresFile;
    private Path testSpecFile;
    @BeforeEach
    public void setUp() throws IOException {
        deleteAppDataFolder();
        prepareFeatureFile();
        var filePath = Paths.get(TEST_CASE_SPEC_FILE);
        testSpecFile = Files.createFile(filePath);
        assertThat(testSpecFile).isNotNull();
        assertThat(Files.exists(testSpecFile)).isTrue();
        Files.write(testSpecFile, List.of(testCaseSpecJson.split("\n")));
        try (var stream = Files.lines(testSpecFile)) {
            assertThat(stream.count()).isGreaterThan(0);
        }
    }
    private void prepareFeatureFile() throws IOException {
        var filePath = Paths.get(OSQAConfig.MODULE_FILE);
        Files.createDirectory(Paths.get("data"));
        featuresFile = Files.createFile(filePath);
        assertThat(featuresFile).isNotNull();
        assertThat(Files.exists(featuresFile)).isTrue();
        Files.write(featuresFile, List.of(featuresJson.split("\n")));
        try (var stream = Files.lines(featuresFile)) {
            assertThat(stream.count()).isGreaterThan(0);
        }
    }
    @Test
    public void shouldLoadFeaturesListFileTest() {
        Result<Void> result = OSQAConfig.loadFeaturesListFile();
        System.out.println(result);
        assertThat(result instanceof Result.Success<Void>).isTrue();
    }
    @Test
    public void shouldComposeFeatureListTest() {
        Result<Void> result = OSQAConfig.loadFeaturesListFile();
        assertThat(result instanceof Result.Success).isTrue();
        Result<OSQAFeature> loadFeaturesResult = OSQAConfig.loadFeature(OSQAConfig.MODULE_FILE);
        assertThat(loadFeaturesResult instanceof Result.Success<OSQAFeature>).isTrue();
        var calendarAndNavFeature = ((Result.Success<OSQAFeature>) loadFeaturesResult).value();
        assertThat(calendarAndNavFeature).isNotNull();
        assertThat(calendarAndNavFeature);
        assertThat(calendarAndNavFeature).isNotNull();
        assertThat(calendarAndNavFeature.uuid()).isEqualTo("a76b4d46-e7df-43ea-afec-221b899ae527");
        assertThat(calendarAndNavFeature.name()).isEqualTo("Core Calendar and Navigation");
        assertThat(calendarAndNavFeature.description()).isEqualTo("Validates basic calendar rendering, navigation controls, and fundamental UI elements.");
        assertThat(calendarAndNavFeature.priority()).isEqualTo("Critical");
        List<OSQATestCase> testCases = calendarAndNavFeature.testCases();
        assertThat(testCases).isNotNull();
        assertThat(testCases).isNotEmpty();
        Optional<OSQATestCase> testCase = testCases.stream().findFirst();
        assertThat(testCase).isNotNull();
        assertThat(testCase).isNotEmpty();
        assertThat(testCase.get().uuid()).isEqualTo("0b8c4bf2-4590-4b01-bda2-cf7271a76789");
        assertThat(testCase.get().title()).isEqualTo("Smoke - Create Daily Task");
        assertThat(testCase.get().specFile()).isEqualTo("tc-smoke-001.json");
    }
    @Test
    public void shouldLoadTestSpecificationTest(){
        var expectedTestSpec = new OSQATestSpec(
                "a06e2598-bed3-4393-b6a2-9645b6bfa294",
                "On Device B, mark the 'Team Sync' task as complete.",
                List.of(
                        new OSQAVerification(1,"On Device B, the task is marked complete and a new instance appears with the correct future date."),
                        new OSQAVerification(2,"On Device A, after a sync/refresh, the original task is marked complete and the new instance appears with the correct future date.")
                ));
        var testCase = new OSQATestCase(
                "47196d64-56f8-4ad3-b96e-24acbc907af7",
                "Task Completion Sync",
                TEST_CASE_SPEC_FILE);
        Result<OSQATestSpec> result = OSQAConfig.loadTestCaseSpec(testCase);
        IO.println(result);
        assertThat(result instanceof Result.Success<OSQATestSpec>).isTrue();
        OSQATestSpec actualTestSpec = ((Result.Success<OSQATestSpec>) result).value();
        assertThat(actualTestSpec).isNotNull();
        assertThat(actualTestSpec.action()).isEqualTo(expectedTestSpec.action());
        assertThat(actualTestSpec.verifications().size()).isEqualTo(expectedTestSpec.verifications().size());
        assertThat(actualTestSpec.verifications()).isNotEmpty();
        assertThat(actualTestSpec.verifications().getFirst().order()).isEqualTo(1);
        assertThat(actualTestSpec.verifications().getFirst().description()).isEqualTo("On Device B, the task is marked complete and a new instance appears with the correct future date.");
        assertThat(actualTestSpec.verifications().getLast().order()).isEqualTo(2);
        assertThat(actualTestSpec.verifications().getLast().description()).isEqualTo("On Device A, after a sync/refresh, the original task is marked complete and the new instance appears with the correct future date.");
    }
    @Test
    public void shouldRejectInvalidJsonFieldsTest() throws IOException{
        Files.deleteIfExists(featuresFile);
        var filePath = Paths.get(OSQAConfig.MODULE_FILE);
        featuresFile = Files.createFile(filePath);
        assertThat(featuresFile).isNotNull();
        assertThat(Files.exists(featuresFile)).isTrue();
        Files.write(featuresFile, List.of(invalidFeaturesJson.split("\n")));
        try(var stream = Files.lines(featuresFile)){
            assertThat(stream.count()).isGreaterThan(0);
        }
        Result<Void> result = OSQAConfig.loadFeaturesListFile();
        assertThat(result instanceof Result.Success).isTrue();
        assertThatThrownBy(() -> OSQAConfig.loadFeature(OSQAConfig.MODULE_FILE))
                .isInstanceOf(ValueInstantiationException.class);
    }
    @Test
    public void shouldGenerateTimestampedFeatureFileNameTest(){
        var expectedFileName = "2025-11-20-08-34-40.json";
        var extension = "json";
        var created = LocalDateTime.of(2025,11,20,8,34,40);
        String actualFileName = OSQAConfig.timestampedName(created,extension);
        assertThat(actualFileName).isNotEmpty();
        assertThat(actualFileName).isEqualTo(expectedFileName);
    }
    @Test
    public void shouldWriteSpecFileTest() throws IOException {
        var uuid = "5833312b-7c84-4e6d-a067-622eb2156761";
        var verification = new OSQAVerification(0,"verification step");
        var specification = new OSQATestSpec(uuid,"Launch application",List.of(verification));
        var timestamp = LocalDateTime.of(2000,11,21,10,55,30);
        var specFile = OSQAConfig.timestampedName(timestamp,"json");
        var result = OSQAConfig.writeSpecFile(Paths.get(OSQAConfig.MODULE_DIR),specification,specFile);
        IO.println(result);
        assertThat(result instanceof Result.Success<Void>).isTrue();
        Files.deleteIfExists(Paths.get(specFile));
    }
    @Test
    public void shouldWriteFeaturesConfFileTest() throws IOException {
        var uuid = "5833312b-7c84-4e6d-a067-622eb2156761";
        var testSpec = new OSQATestCase(uuid,"testcase","specfile.json");
        var feature = new OSQAFeature(uuid,"Launch application","Feature notes","Critical",List.of(testSpec));
        var result = OSQAConfig.writeFeature(Paths.get(OSQAConfig.MODULE_DIR),feature);
        IO.println(result);
        assertThat(result instanceof Result.Success<Path>).isTrue();
        var path = ((Result.Success<Path>) result).value();
        assertThat(Files.exists(path)).isTrue();
        IO.println(path.getFileName());
        Files.deleteIfExists(((Result.Success<Path>) result).value());
    }
    @Test
    public void shouldFindAndListFeatureConfFilesTest() throws IOException {
        var uuid = "5833312b-7c84-4e6d-a067-622eb2156761";
        var featureTitle = "Launch application";
        var testSpec = new OSQATestCase(uuid,"testcase","specfile.json");
        var feature = new OSQAFeature(uuid,featureTitle,"Feature notes","Critical",List.of(testSpec));
        var result = OSQAConfig.writeFeature(Paths.get(OSQAConfig.MODULE_DIR),feature);
        IO.println(result);
        assertThat(result instanceof Result.Success<Path>).isTrue();
        var path = ((Result.Success<Path>) result).value();
        assertThat(Files.exists(path)).isTrue();
        Result<List<OSQAFeature>> featuresResult = OSQAConfig.listFeatures(Paths.get(OSQAConfig.MODULE_DIR));
        IO.println(featuresResult);
        assertThat(featuresResult instanceof Result.Success<List<OSQAFeature>>).isTrue();
        var features = ((Result.Success<List<OSQAFeature>>) featuresResult).value();
        assertThat(features).isNotEmpty();
        assertThat(features.getFirst()).isNotNull();
        assertThat(features.getFirst().name()).isNotEmpty();
        assertThat(features.getFirst().description()).isNotEmpty();
        Files.deleteIfExists(path);
    }
    @Test
    public void shouldOverwriteSpecFileTest(){
        var uuid = "5833312b-7c84-4e6d-a067-622eb2156761";
        var verification = new OSQAVerification(0,"verification step");
        var specification = new OSQATestSpec(uuid,"Launch application",List.of(verification));
        var timestamp = LocalDateTime.of(2000,11,21,10,55,30);
        var specFile = OSQAConfig.timestampedName(timestamp,"json");
        var appDir = Paths.get(OSQAConfig.MODULE_DIR);
        var filePath = appDir.toAbsolutePath().toString().concat("/").concat(specFile);
        var testCase = new OSQATestCase(uuid,"Test Case",filePath);
        var result = OSQAConfig.writeSpecFile(appDir,specification,specFile);
        IO.println(result);
        assertThat(result instanceof Result.Success<Void>).isTrue();
        var preOverwriteTestSpecLoadResult = OSQAConfig.loadTestCaseSpec(testCase);
        IO.println("load pre overwrite test spec: result : " + preOverwriteTestSpecLoadResult);
        assertThat(preOverwriteTestSpecLoadResult instanceof Result.Success<OSQATestSpec>).isTrue();
        OSQATestSpec preOverwriteSpec = ((Result.Success<OSQATestSpec>) preOverwriteTestSpecLoadResult).value();
        assertThat(preOverwriteSpec).isNotNull();
        assertThat(preOverwriteSpec.verifications().size()).isEqualTo(1);
        assertThat(preOverwriteSpec.verifications().getFirst()).isNotNull();
        assertThat(preOverwriteSpec.verifications().getFirst().order()).isEqualTo(verification.order());
        assertThat(preOverwriteSpec.verifications().getFirst().description()).isEqualTo(verification.description());

        var newVerification = new OSQAVerification(0,"verification step");
        var updatedSpec = new OSQATestSpec(uuid,"Launch application",List.of(verification,newVerification));

        var overwriteResult = OSQAConfig.overwriteSpecFile(updatedSpec,testCase);
        assertThat(overwriteResult instanceof Result.Success<Void>).isTrue();
        var testSpecLoadResult = OSQAConfig.loadTestCaseSpec(testCase);
        IO.println(result);
        assertThat(testSpecLoadResult instanceof Result.Success<OSQATestSpec>).isTrue();
        OSQATestSpec specOverwrite = ((Result.Success<OSQATestSpec>) testSpecLoadResult).value();
        assertThat(specOverwrite).isNotNull();
        assertThat(specOverwrite.action()).isEqualTo(updatedSpec.action());
        assertThat(specOverwrite.verifications().size()).isEqualTo(updatedSpec.verifications().size());
        assertThat(specOverwrite.verifications().size()).isGreaterThan(1);
        assertThat(specOverwrite.verifications().size()).isEqualTo(2);
    }
    @AfterEach
    public void tearDown() throws IOException {
        deleteAppDataFolder();
    }
    private static void deleteAppDataFolder() throws IOException {
        var directory = Paths.get("data");
        if (Files.exists(directory)){
            try(var dirWalk = Files.walk(directory)){
                dirWalk.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        }
    }
    private final String featuresJson = """
            {
                "uuid": "a76b4d46-e7df-43ea-afec-221b899ae527",
                "name": "Core Calendar and Navigation",
                "description": "Validates basic calendar rendering, navigation controls, and fundamental UI elements.",
                "priority": "Critical",
                "testCases": [
                  {
                    "uuid": "0b8c4bf2-4590-4b01-bda2-cf7271a76789",
                    "title": "Smoke - Create Daily Task",
                    "specFile": "tc-smoke-001.json"
                  }
                ]
              }
            """;
    private final String invalidFeaturesJson = """
            {
                "uuid": "uuid",
                "name": "",
                "description": "",
                "priority": "",
                "testCases": [
                  {
                    "uuid": "uuid",
                    "title": "Smoke - Create Daily Task",
                    "specFile": "tc-smoke-001.json"
                  }
                ]
              }
            """;
    private final String testCaseSpecJson = """
            {
                "uuid": "a06e2598-bed3-4393-b6a2-9645b6bfa294",
                "action": "On Device B, mark the 'Team Sync' task as complete.",
                "verifications": [
                  {
                    "order": 1,
                    "description": "On Device B, the task is marked complete and a new instance appears with the correct future date."
                  },
                  {
                    "order": 2,
                    "description": "On Device A, after a sync/refresh, the original task is marked complete and the new instance appears with the correct future date."
                  }
                ]
            }
            """;
}
