module stamfordcoop {
	exports fertilizer;

	requires transitive commons.math3;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.fxml;
	requires transitive javafx.graphics;

	requires com.google.common;
	requires java.desktop;
	opens fertilizer;
}