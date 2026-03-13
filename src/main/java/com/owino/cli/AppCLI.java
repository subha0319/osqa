package com.owino.cli;
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
import com.owino.core.OSQAModel.OSQATestCase;
import com.owino.core.Result;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import com.owino.core.OSQAModel.OSQAFeature;
import com.owino.core.OSQAModel.OSQATestSpec;
import com.owino.core.OSQAModel.OSQAOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class AppCLI {
    private final Logger LOG = LoggerFactory.getLogger(AppCLI.class);
    void main() {
        IO.println("""
                 _____ _____  _____  ___
                |  _  /  ___||  _  |/ _ \\
                | | | \\ `--. | | | / /_\\ \\
                | | | |`--. \\| | | |  _  |
                \\ \\_/ /\\__/ /\\ \\/' / | | |
                 \\___/\\____/  \\_/\\_\\_| |_/
                
                Welcome to OSQA!
                """);
        var session = new OSQASession(new Scanner(System.in));
        Optional<String> featuresFile;
        do {
            featuresFile = switch (OSQAConfig.loadFeaturesListFile()) {
                case Result.Success<Void> _ -> Optional.of(OSQAConfig.MODULE_FILE);
                case Result.Failure (Throwable failure) -> {
                    LOG.error("Didn't find pre-existing features config: {}", failure.getLocalizedMessage());
                    yield Optional.empty();
                }
            };
            try {
                if (featuresFile.isEmpty() || Files.readString(Paths.get(featuresFile.get())).isBlank()){
                    session.generateTestConfig();
                }
            } catch (IOException failure){
                IO.println("Features config file is empty");
                IO.println("Setup a features config for your tests:");
                featuresFile = Optional.empty();
            } finally {
                if (featuresFile.isEmpty())
                    session.generateTestConfig();
            }
        } while (featuresFile.isEmpty());
        var feature = switch (OSQAConfig.loadFeature(featuresFile.get())) {
            case Result.Success<OSQAFeature> (OSQAFeature featureValue) -> featureValue;
            case Result.Failure (Throwable failure) -> throw new RuntimeException(failure);
        };
        var selectedFeature = switch (session.featureSelection(List.of(feature))){
            case Result.Success<OSQAFeature> success -> success.value();
            case Result.Failure (Throwable failure) -> throw new RuntimeException(failure);
        };
        IO.println("Selected Feature -> " + selectedFeature.name());
        List<OSQAOutcome> testSessionReport = new ArrayList<>();
        for (OSQATestCase testCase : selectedFeature.testCases()) {
            Optional<OSQATestSpec> optionalTestSpec = switch(OSQAConfig.loadTestCaseSpec(testCase)) {
                case Result.Success<OSQATestSpec> success -> Optional.of(success.value());
                case Result.Failure (Throwable failure) -> {
                    LOG.error(failure.getLocalizedMessage());
                    LOG.error("Moving to next test spec");
                    yield Optional.empty();
                }
            };
            if (optionalTestSpec.isEmpty()){
                LOG.error("This test case does not contain a valid test spec");
                LOG.error("Moving on to next test case");
                continue;
            }
            var testOutcomes = session.verifyQATestSpec(optionalTestSpec.get());
            testSessionReport.addAll(testOutcomes);
        }
        IO.println("QA Session Completed!");
        IO.println("QA SESSION RESULTS:");
        testSessionReport.forEach(outcome -> IO.println("""
                Verification: %s
                Verification Result: %s
                """.formatted(outcome.verification(),outcome.passedTest() ? "Passed Test ✅" : "Failed Test ❌")));

    }
}
