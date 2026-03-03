package com.owino.desktop;
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
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
public class DashboardView extends SplitPane {
    public DashboardView(Stage stage){
        initView(stage);
    }
    private void initView(Stage stage) {
        getItems().add(new MainMenuView());
        getItems().add(new WelcomeView(stage));
        addEventHandler(AppEvents.OPEN_MODULE_FORM_EVENT, _ -> {
            getItems().removeFirst();
            getItems().removeLast();
            getItems().add(new MainMenuView());
            getItems().add(new VerificationFormView());
        });
        addEventHandler(AppEvents.CLOSE_MODULE_FORM_EVENT, _ -> {
            getItems().removeFirst();
            getItems().removeLast();
            getItems().add(new MainMenuView());
            getItems().add(new WelcomeView(stage));
        });
        setOrientation(Orientation.HORIZONTAL);
        setDividerPositions(0.1f);
        setStyle("-fx-divider-color: #cccccc; -fx-divider-width: 1;");
    }
}
