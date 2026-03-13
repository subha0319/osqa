package com.owino.desktop.dashboard;
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
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import com.owino.desktop.OSQANavigationEvents.OpenDashboardEvent;
import com.owino.desktop.OSQANavigationEvents.OpenFeatureFormEvent;
import com.owino.desktop.OSQANavigationEvents.ShowVerificationFormEvent;
import com.owino.desktop.OSQANavigationEvents.ResetVerificationsEvent;
import com.owino.desktop.OSQANavigationEvents.ToggleShowVerificationButtonEvent;
public class AppToolbar extends BorderPane {
    private final HBox rightMostContainer;
    private final Button addVerificationButton;
    private final Button resetVerificationButton;
    public AppToolbar(){
        var brandLabel = new Label("OSQA");
        addVerificationButton = new Button("New Verification");
        var addFeatureButton = new Button("New Feature");
        var homeButton = new Button("Home");
        var addProductButton = new Button("New Product");
        resetVerificationButton = new Button("Reset");
        styleBrandLabel(brandLabel);
        rightMostContainer = new HBox();
        rightMostContainer.getChildren().add(addFeatureButton);
        rightMostContainer.getChildren().add(addProductButton);
        rightMostContainer.getChildren().add(homeButton);
        HBox.setMargin(addFeatureButton, new Insets(6));
        HBox.setMargin(addProductButton, new Insets(6));
        HBox.setMargin(homeButton, new Insets(6));
        setRight(rightMostContainer);
        setLeft(brandLabel);
        setMargin(rightMostContainer,new Insets(6));
        setMargin(brandLabel,new Insets(6));
        homeButton.setOnAction(_ -> {
            EventBus.getDefault().post(new ToggleShowVerificationButtonEvent(false));
            EventBus.getDefault().post(new OpenDashboardEvent());
        });
        addFeatureButton.setOnAction(_ -> {
            EventBus.getDefault().post(new ToggleShowVerificationButtonEvent(false));
            EventBus.getDefault().post(new OpenFeatureFormEvent());
        });
        brandLabel.setOnMouseClicked(_ -> {
            EventBus.getDefault().post(new OpenDashboardEvent());
            EventBus.getDefault().post(new ToggleShowVerificationButtonEvent(false));

        });
        addVerificationButton.setOnAction(_ -> EventBus.getDefault().post(new ShowVerificationFormEvent()));
        resetVerificationButton.setOnAction(_ -> EventBus.getDefault().post(new ResetVerificationsEvent()));
        EventBus.getDefault().register(this);
    }
    private void styleBrandLabel(Label brandLabel) {
        brandLabel.setFont(Font.font(21));
        brandLabel.setTextFill(Color.BLUE);
    }
    @Subscribe
    public void showVerificationButton(ToggleShowVerificationButtonEvent event){
        Platform.runLater(() -> {
            rightMostContainer.getChildren().remove(addVerificationButton);
            rightMostContainer.getChildren().remove(resetVerificationButton);
            if (event.show()) {
                rightMostContainer.getChildren().add(addVerificationButton);
                rightMostContainer.getChildren().add(resetVerificationButton);
                HBox.setMargin(addVerificationButton, new Insets(6));
                HBox.setMargin(resetVerificationButton, new Insets(6));
            }
        });
    }
}
