module stamfordcoop {
	exports fertilizer;

	requires transitive org.apache.commons.math4.legacy;
	requires transitive org.apache.commons.math4.legacy.core;
	requires org.apache.commons.math4.legacy.exception;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.fxml;
	requires transitive javafx.graphics;

	requires java.desktop;
    requires com.google.gson;
    requires java.logging;
    requires transitive java.prefs;
    requires com.github.librepdf.openpdf;
    requires org.apache.pdfbox;
	opens fertilizer;
}