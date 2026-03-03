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
import com.owino.core.Result;
import com.owino.settings.SettingDao;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
public class WelcomeView extends StackPane {
    public WelcomeView(Stage window){
        var itemsContainer = new VBox(22);
        var welcomeAscii = """
                 _____ _____  _____  ___
                |  _  /  ___||  _  |/ _ \\
                | | | \\ `--. | | | / /_\\ \\
                | | | |`--. \\| | | |  _  |
                \\ \\_/ /\\__/ /\\ \\/' / | | |
                 \\___/\\____/  \\_/\\_\\_| |_/
                
                Welcome to OSQA!
                """;
        itemsContainer.getChildren().add(new Text(welcomeAscii));
        itemsContainer.getChildren().add(new Text("Create new Module or Select Module to Proceed!"));
        var addModuleButton = new Button("Add New Verification");
        var settingsButton = new Button("Set OSQA Output Folder");
        var buttonsContainer = new HBox(21);
        buttonsContainer.getChildren().add(settingsButton);
        buttonsContainer.getChildren().add(addModuleButton);
        itemsContainer.getChildren().add(buttonsContainer);
        getChildren().add(itemsContainer);
        setMargin(itemsContainer, new Insets(45));
        var appDataFolderLabel = new Text();
        settingsButton.setOnAction( _ -> {
            var directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select config destination:");
            File selectedDir = directoryChooser.showDialog(window);
            if (selectedDir != null) {
                IO.println("Selected Dir " + selectedDir.getAbsolutePath());
                var result = SettingDao.setAppDataDir(selectedDir);
                if (result instanceof Result.Success<Void>) {
                    IO.println("Dir saved successfully!");
                    loadAppDir();
                } else IO.println(result);
            }
        });
        addModuleButton.setOnAction(_ -> {
            fireEvent(AppEvents.openModuleFormEvent(UUID.randomUUID().toString(),false));
        });
        addEventHandler(AppEvents.APP_DIR_LOADED_EVENT,event -> {
            IO.println("received app dir update event...");
            if (event.appDir != null) {
                appDataFolderLabel.setText("Config Folder: " + event.appDir.toAbsolutePath());
                itemsContainer.getChildren().remove(appDataFolderLabel);
                itemsContainer.getChildren().add(appDataFolderLabel);
            }
        });
        loadAppDir();
    }
    private void loadAppDir() {
        var result = SettingDao.getAppDataDir();
        if (result instanceof Result.Success<Path> (Path path)){
            fireEvent(AppEvents.appDirLoadedEvent(path));
        }
    }
}
