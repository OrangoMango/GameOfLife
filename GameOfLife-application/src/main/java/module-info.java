// File managed by WebFX (DO NOT EDIT MANUALLY)

module GameOfLife.application {

    // Direct dependencies modules
    requires javafx.base;
    requires javafx.graphics;
    requires webfx.extras.filepicker;
    requires webfx.platform.blob;
    requires webfx.platform.console;
    requires webfx.platform.file;
    requires webfx.platform.resource;

    // Exported packages
    exports com.orangomango.gameoflife;

    // Resources packages
    opens images;

    // Provided services
    provides javafx.application.Application with com.orangomango.gameoflife.MainApplication;

}