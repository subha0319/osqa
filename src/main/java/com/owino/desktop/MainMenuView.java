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
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
public class MainMenuView extends VBox {
    public MainMenuView(){
        setMinWidth(200);
        setMaxWidth(300);
        setStyle("-fx-background-color: #2c3e50;");
        var moduleTitleView = new Text("OSQA");
        var menuItemMargin = new Insets(12,12,12,12);
        moduleTitleView.setFont(Font.font(21));
        getChildren().add(moduleTitleView);
        setMargin(moduleTitleView,menuItemMargin);
    }
}
