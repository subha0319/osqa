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
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
public class AppGUI extends Application {
    @Override
    public void start(Stage stage) {
        var dashboard = new DashboardView(stage);
        var scene = new Scene(dashboard);
        stage.setScene(scene);
        stage.setMinHeight(800);
        stage.setMinWidth(1000);
        stage.setTitle("OSQA");
        stage.setOnShown(_ -> IO.println("onAppear..."));
        stage.show();
    }
}
