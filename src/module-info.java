module IOClient {
	requires javafx.controls;
	
	opens application to javafx.graphics, javafx.fxml;
}
