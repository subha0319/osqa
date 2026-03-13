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
import com.owino.desktop.OSQANavigationEvents;
import com.owino.core.Result;
import com.owino.settings.SettingDao;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import java.io.File;
import java.nio.file.Path;
public class WelcomeView extends StackPane {
    private Label newProjectLabel;
    private VBox itemsContainer;
    private Button settingsButton;
    private HBox buttonsContainer;
    private Text appDataFolderLabel;
    private final Stage window;
    public WelcomeView(Stage window){
        this.window = window;
        EventBus.getDefault().register(this);
        initView();
        loadAppDir();
    }
    private void initView() {
        var welcomeAscii = """
                 _____ _____  _____  ___
                |  _  /  ___||  _  |/ _ \\
                | | | \\ `--. | | | / /_\\ \\
                | | | |`--. \\| | | |  _  |
                \\ \\_/ /\\__/ /\\ \\/' / | | |
                 \\___/\\____/  \\_/\\_\\_| |_/
                
                Welcome to OSQA!
                """;
        itemsContainer = new VBox(22);
        newProjectLabel = new Label("Create new Feature or Select Feature to Proceed!");
        var ascii = new Text(welcomeAscii);
        ascii.setFont(Font.font(25));
        ascii.setFill(Color.BLUE);
        itemsContainer.getChildren().add(ascii);
        settingsButton = new Button("Set OSQA Output Folder");
        buttonsContainer = new HBox(21);
        itemsContainer.getChildren().add(buttonsContainer);
        getChildren().add(itemsContainer);
        setMargin(itemsContainer, new Insets(45));
        appDataFolderLabel = new Text();
        settingsButton.setOnAction( _ -> {
            var directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select config destination:");
            File selectedDir = directoryChooser.showDialog(window);
            if (selectedDir != null) {
                var result = SettingDao.setAppDataDir(selectedDir);
                if (result instanceof Result.Success<Void>) {
                    loadAppDir();
                } else IO.println(result);
            }
        });
    }
    private void loadAppDir() {
        var result = SettingDao.getAppDataDir();
        if (result instanceof Result.Success<Path> (Path path)){
            itemsContainer.getChildren().remove(newProjectLabel);
            EventBus.getDefault().post(new OSQANavigationEvents.AppDirEvent(path));
        } else {
            itemsContainer.getChildren().add(newProjectLabel);
            itemsContainer.getChildren().remove(newProjectLabel);
            buttonsContainer.getChildren().add(settingsButton);
        }
    }
    @Subscribe
    public void appDirLoadedEvent(OSQANavigationEvents.AppDirEvent event){
        Platform.runLater(() -> {
            if (event.appDir() != null) {
                appDataFolderLabel.setText("Project Folder: " + event.appDir().toAbsolutePath());
                itemsContainer.getChildren().remove(appDataFolderLabel);
                itemsContainer.getChildren().add(appDataFolderLabel);
            }
        });
    }
}
