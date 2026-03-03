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
import javafx.event.Event;
import javafx.event.EventType;
import java.nio.file.Path;
public class AppEvents extends Event {
    public static final EventType<AppEvents> OPEN_MODULE_FORM_EVENT = new EventType<>(Event.ANY,"OPEN_FORM_ACTION");
    public static final EventType<AppEvents> CLOSE_MODULE_FORM_EVENT = new EventType<>(Event.ANY,"CLOSE_FORM_ACTION");
    public static final EventType<AppEvents> SHOW_MODULE_SAVE_BUTTON_EVENT = new EventType<>(Event.ANY,"SHOW_MODULE_SAVE_ACTION");
    public static final EventType<AppEvents> ADD_TEST_CASE_VERIFICATION_EVENT = new EventType<>(Event.ANY,"ADD_TEST_VERIFICATION_ACTION");
    public static final EventType<AppEvents> APP_DIR_LOADED_EVENT = new EventType<>(Event.ANY,"APP_DIR_LOADED_ACTION");
    public final String moduleUuid;
    public final boolean moduleFormEditMode;
    public Path appDir = null;
    public AppEvents(EventType<? extends Event> eventType, String moduleUuid, boolean moduleFormEditMode) {
        super(eventType);
        this.moduleUuid = moduleUuid;
        this.moduleFormEditMode = moduleFormEditMode;
    }
    public AppEvents(EventType<? extends Event> eventType) {
        super(eventType);
        this.moduleUuid = "";
        this.moduleFormEditMode = false;
    }
    public AppEvents(EventType<? extends Event> eventType, Path appDir) {
        super(eventType);
        this.moduleUuid = "";
        this.moduleFormEditMode = false;
        this.appDir = appDir;
    }
    public static AppEvents openModuleFormEvent(String moduleUuid,boolean openEditMode){
        return new AppEvents(OPEN_MODULE_FORM_EVENT,moduleUuid,openEditMode);
    }
    public static AppEvents closeModuleFormEvent(){
        return new AppEvents(CLOSE_MODULE_FORM_EVENT);
    }
    public static AppEvents showModuleSaveButton(){
        return new AppEvents(SHOW_MODULE_SAVE_BUTTON_EVENT);
    }
    public static AppEvents addTestVerification(){
        return new AppEvents(ADD_TEST_CASE_VERIFICATION_EVENT);
    }
    public static AppEvents appDirLoadedEvent(Path appDir) { return new AppEvents(APP_DIR_LOADED_EVENT,appDir); }
}
