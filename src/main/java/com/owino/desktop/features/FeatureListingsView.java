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
import com.owino.core.Result;
import javafx.geometry.Insets;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import com.owino.core.OSQAConfig;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.greenrobot.eventbus.EventBus;
import com.owino.settings.SettingDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.owino.desktop.OSQANavigationEvents;
import com.owino.core.OSQAModel.OSQAFeature;
public class FeatureListingsView extends VBox {
    public FeatureListingsView(){
        var appDirResult = SettingDao.getAppDataDir();
        Optional<Path> featuresDir = switch (appDirResult){
            case Result.Success<Path> (Path appDir) -> Optional.of(appDir);
            case Result.Failure<Path> failure -> {
                IO.println(failure.error().getLocalizedMessage());
                yield Optional.empty();
            }
        };
        if (featuresDir.isPresent()) {
            List<OSQAFeature> features = switch (OSQAConfig.listFeatures(featuresDir.get())){
                case Result.Success<List<OSQAFeature>> (List<OSQAFeature> featuresValue) -> featuresValue;
                case Result.Failure<List<OSQAFeature>> failure -> {
                    IO.println("Failed to load feature list:" + failure.error().getLocalizedMessage());
                    yield List.of();
                }
            };
            if (!features.isEmpty()){
                ObservableList<OSQAFeature> listViewContents = FXCollections.observableList(features);
                var listView = new ListView<OSQAFeature>(listViewContents);
                listView.setCellFactory(item -> new ListCell<>(){
                    @Override
                    protected void updateItem(OSQAFeature feature, boolean empty) {
                        super.updateItem(feature, empty);
                        if (empty || feature == null){
                            setText("");
                            setGraphic(null);
                        } else {
                            var featureItemContainer = new VBox(10);
                            var nameLabel = new Label(feature.name());
                            var descriptionLabel = new Label(feature.description());
                            descriptionLabel.setMaxWidth(700);
                            descriptionLabel.setWrapText(true);
                            featureItemContainer.getChildren().addAll(nameLabel, descriptionLabel, new Separator());
                            VBox.setMargin(nameLabel,new Insets(12,12,3,12));
                            VBox.setMargin(descriptionLabel,new Insets(3,12,6,12));
                            var blueBackground = new Background(new BackgroundFill(Color.BLUE,new CornerRadii(12),Insets.EMPTY));
                            var blackBackground = new Background(new BackgroundFill(Color.BLACK,new CornerRadii(12),Insets.EMPTY));
                            featureItemContainer.setOnMouseEntered(_ -> featureItemContainer.setBackground(blueBackground));
                            featureItemContainer.setOnMouseExited(_ -> featureItemContainer.setBackground(blackBackground));
                            setGraphic(featureItemContainer);
                        }
                    }
                });
                listView.setBorder(Border.EMPTY);
                var featureSelectionModel = listView.getSelectionModel();
                featureSelectionModel.setSelectionMode(SelectionMode.SINGLE);
                var featureSelectedItemProp = featureSelectionModel.selectedItemProperty();
                featureSelectedItemProp.addListener((_, _,selectedFeature) -> {
                    if (selectedFeature != null){
                        EventBus.getDefault().post(new OSQANavigationEvents.OpenFeatureDetailedViewEvent(selectedFeature));
                    }
                });
                var listViewContainer = new VBox(listView);
                getChildren().add(listViewContainer);
                setMargin(listViewContainer,new Insets(12,12,12,12));
            }
        }
    }
}
