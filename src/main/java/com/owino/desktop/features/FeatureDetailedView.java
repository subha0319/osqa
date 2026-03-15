package com.owino.desktop.features;
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
import java.util.*;
import com.owino.core.Result;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.scene.layout.VBox;
import com.owino.core.OSQAConfig;
import javafx.scene.layout.Border;
import javafx.application.Platform;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.EventBus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.owino.core.OSQAModel.OSQAFeature;
import com.owino.core.OSQAModel.OSQATestSpec;
import com.owino.core.OSQAModel.OSQATestCase;
import com.owino.desktop.OSQANavigationEvents;
import com.owino.core.OSQAModel.OSQAVerification;
import com.owino.desktop.OSQANavigationEvents.ResetVerificationsEvent;
import com.owino.desktop.OSQANavigationEvents.ShowVerificationFormEvent;
import com.owino.desktop.OSQANavigationEvents.ToggleShowVerificationButtonEvent;
public class FeatureDetailedView extends VBox {
    public static final Insets MARGIN = new Insets(8,22,8,22);
    private final ObservableList<OSQAVerification> observableVerificationsList = FXCollections.observableArrayList();
    private ListView<OSQAVerification> verificationsListView;
    private final OSQAFeature feature;
    private OSQATestSpec testSpec;
    private final OSQATestCase testCase;
    public FeatureDetailedView(OSQAFeature feature){
        this.feature = feature;
        var featureTitleLabel = new Label();
        var featureDescriptionLabel = new Label();
        var featureUsageInstructions = new Label();
        featureTitleLabel.setText(this.feature.name());
        featureDescriptionLabel.setText(this.feature.description());
        featureDescriptionLabel.setWrapText(true);
        featureTitleLabel.setFont(Font.font(47));
        featureTitleLabel.setFont(Font.font(15));
        var testCases = this.feature.testCases();
        testCase = testCases.getFirst();
        Optional<OSQATestSpec> optionalTestSpect = switch (OSQAConfig.loadTestCaseSpec(testCase)){
            case Result.Success<OSQATestSpec> (OSQATestSpec testSpec) -> Optional.of(testSpec);
            case Result.Failure<OSQATestSpec> failure -> {
                IO.println("Failed to load test spec for test case " + testCase.title() + " " + failure.error().getLocalizedMessage());
                yield Optional.empty();
            }
        };
        if (optionalTestSpect.isPresent()){
            testSpec = optionalTestSpect.get();
            featureUsageInstructions.setText(testSpec.action());
            verificationsListView = new ListView<>(observableVerificationsList);
            verificationsListView.setCellFactory(_ -> new ListCell<>(){
                @Override
                protected void updateItem(OSQAVerification verification, boolean empty) {
                    super.updateItem(verification, empty);
                    if (empty || verification == null){
                        setText("");
                        setGraphic(null);
                    } else {
                        var container = new VBox();
                        var verificationCheckbox = new CheckBox(verification.description());
                        verificationCheckbox.setSelected(verification.verificationStatus());
                        verificationCheckbox.setWrapText(true);
                        container.getChildren().add(verificationCheckbox);
                        VBox.setMargin(verificationCheckbox,MARGIN);
                        verificationCheckbox.selectedProperty().addListener((observableValue,_,newVerifiedStatus) -> {
                            var updatedVerification = new OSQAVerification(verification.uuid(),verification.order(),verification.description(),newVerifiedStatus);
                            switch (OSQAConfig.updateVerificationStatus(testSpec,testCase,updatedVerification)){
                                case Result.Success<OSQATestSpec> (OSQATestSpec updatedTestSpec) -> {
                                    testSpec = updatedTestSpec;
                                    var alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("Success!");
                                    alert.setContentText("Verification Updated!");
                                    alert.show();
                                    reloadVerifications();
                                }
                                case Result.Failure<OSQATestSpec> failure -> {
                                    var alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setContentText("""
                                            Verification Update Failed!
                                            Error: %s
                                            """.formatted(failure.error().getLocalizedMessage()));
                                    alert.show();
                                }
                            }
                        });
                        setGraphic(container);
                    }
                }
            });
            verificationsListView.setBorder(Border.EMPTY);
        }
        getChildren().add(featureTitleLabel);
        getChildren().add(featureDescriptionLabel);
        getChildren().add(featureUsageInstructions);
        if (verificationsListView != null)
            getChildren().add(verificationsListView);
        VBox.setMargin(featureTitleLabel,MARGIN);
        VBox.setMargin(featureUsageInstructions,MARGIN);
        VBox.setMargin(featureDescriptionLabel,MARGIN);
        EventBus.getDefault().register(this);
        EventBus.getDefault().post(new ToggleShowVerificationButtonEvent(true));
        reloadVerifications();
    }
    @Subscribe
    public void showNewVerificationFormEvent(ShowVerificationFormEvent event){
        var dialog = new FeatureVerificationForm();
        Optional<String> inputResult = dialog.showAndWait();
        if (inputResult.isPresent()){
            var verificationDesc = inputResult.get();
            var newVerification = new OSQAVerification(UUID.randomUUID().toString(),0,verificationDesc,false);
            if (testSpec != null){
                var verifications = testSpec.verifications();
                verifications.add(newVerification);
                var updatedTestSpec = new OSQATestSpec(testSpec.uuid(),testSpec.action(),verifications);
                Result<Void> overwriteResult = OSQAConfig.overwriteSpecFile(updatedTestSpec,testCase);
                switch (overwriteResult){
                    case Result.Success<Void> _ -> {
                        var alert = new Alert(Alert.AlertType.NONE);
                        alert.setHeaderText("Verifications for this test have been updated successfully!");
                        var dialogResult = alert.showAndWait();
                        if (dialogResult.isPresent()){
                            EventBus.getDefault().post(new OSQANavigationEvents.OpenFeatureDetailedViewEvent(feature));
                        }
                    }
                    case Result.Failure<Void> failure -> {
                        var alert = new Alert(Alert.AlertType.ERROR);
                        alert.setHeaderText("Failed to add this verification, an error occurred!");
                        alert.setContentText("Cause: " + failure.error().getLocalizedMessage());
                        alert.show();
                    }
                }
            }
        }
    }
    @Subscribe
    public void resetVerificationsEvent(ResetVerificationsEvent event){
        var updatedVerifications = testSpec.verifications().stream()
                .filter(OSQAVerification::verificationStatus)
                .map(verification -> new OSQAVerification(verification.uuid(),verification.order(),verification.description(),false))
                .toList();
        for (OSQAVerification updatedVerification : updatedVerifications) {
            switch (OSQAConfig.updateVerificationStatus(testSpec,testCase,updatedVerification)){
                case Result.Success<OSQATestSpec> (OSQATestSpec updatedTestSpec) -> testSpec = updatedTestSpec;
                case Result.Failure<OSQATestSpec> failure -> IO.println(failure.error().getLocalizedMessage());
            }
        }
        reloadVerifications();
    }
    public void reloadVerifications(){
        Platform.runLater(() -> {
            observableVerificationsList.removeAll();
            observableVerificationsList.clear();
            observableVerificationsList.addAll(testSpec.verifications());
        });
    }
}
