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
import com.owino.core.OSQAModel.OSQAFeature;
import java.nio.file.Path;
import java.util.UUID;
public sealed interface OSQANavigationEvents {
    record OpenDashboardEvent() implements OSQANavigationEvents {}
    record AppDirEvent(Path appDir) implements OSQANavigationEvents {}
    record OpenFeatureFormEvent(String featureUuid,boolean isEditMode) implements OSQANavigationEvents {
        public OpenFeatureFormEvent(){
            this(UUID.randomUUID().toString(),false);
        }
    }
    record OpenFeatureDetailedViewEvent(OSQAFeature selectedFeature) implements OSQANavigationEvents {}
    record OpenFeaturesListViewEvent() implements OSQANavigationEvents {}
    record ToggleShowVerificationButtonEvent(boolean show) implements OSQANavigationEvents {}
    record ShowVerificationFormEvent() implements OSQANavigationEvents {}
    record ResetVerificationsEvent() implements  OSQANavigationEvents {}
}
