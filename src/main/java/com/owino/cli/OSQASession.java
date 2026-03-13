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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import com.owino.core.OSQAConfig;
import com.owino.core.OSQAGenerator;
import com.owino.core.OSQAModel;
import com.owino.core.OSQAModel.OSQAOutcome;
import com.owino.core.OSQAModel.OSQATestSpec;
import com.owino.core.OSQAModel.OSQAVerification;
import com.owino.core.OSQAModel.OSQAFeature;
import com.owino.core.Result;
import tools.jackson.databind.ObjectMapper;
public record OSQASession(Scanner scanner) {
    public void generateTestConfig() {
        var newConfigCreated = false;
        var retryOnFail = false;
        do {
            try {
                var objectMapper = new ObjectMapper().writerWithDefaultPrettyPrinter();
                var featureGenerator = new OSQAGenerator();
                var features = featureGenerator.collectFeatures();
                for (OSQAFeature feature : features) {
                    var featureTestCases = featureGenerator.collectTestCases(feature.name());
                    for (OSQAModel.OSQATestCase testCase : featureTestCases) {
                        var testSpecification = featureGenerator.collectTestCaseSpecs();
                        var specFile = testCase.specFile();
                        Files.writeString(Paths.get(specFile),objectMapper.writeValueAsString(testSpecification));
                    }
                }
                var featureConfFileName = OSQAConfig.timestampedName(LocalDateTime.now(),"json");
                Files.writeString(Paths.get(featureConfFileName),objectMapper.writeValueAsString(features));
                Files.writeString(Paths.get("env.properties"),featureConfFileName);
                newConfigCreated = true;
            } catch (IOException error){
                IO.println("""
                            Error!
                            Failed to create OSQA features config file
                            Experience IO error:
                            %s
                            """.formatted(error.getLocalizedMessage()));
                IO.println("Try again?: y/n");
                retryOnFail = scanner.nextLine().equalsIgnoreCase("y");
            }
        } while (retryOnFail && !newConfigCreated);
    }
    public Result<OSQAFeature> featureSelection(List<OSQAFeature> featureOptions){
        if (featureOptions.isEmpty()) return Result.failure("Feature options list is empty");
        IO.println("Select feature from available options:");
        var index = 0;
        Map<Integer, OSQAFeature> selection = new HashMap<>();
        for (OSQAFeature featureOption : featureOptions) {
            selection.put(index,featureOption);
            index++;
        }
        selection.forEach((selectionIndex,feature) -> IO.println(selectionIndex + " -> " + feature.name() + ":" + feature.description()));
        var featureIndex = scanner.nextInt();
        return Result.success(selection.get(featureIndex));
    }
    public List<OSQAOutcome> verifyQATestSpec(OSQATestSpec testSpec) {
        IO.println("Action -> " + testSpec.action());
        IO.println("""
                    Confirm expected behaviour:
                    0 -> Failed verification
                    1 -> Passed verification
                    """);
        List<OSQAOutcome> outcomes = new ArrayList<>();
        for (OSQAVerification verification : testSpec.verifications()) {
            IO.println(verification.description());
            var passed = scanner.nextInt();
            outcomes.add(new OSQAOutcome(testSpec.uuid(),verification,passed == 1));
        }
        return outcomes;
    }
}
