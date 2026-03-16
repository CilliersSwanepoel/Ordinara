module org.example.ordinara {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens org.example.ordinara to javafx.fxml;
    opens org.example.ordinara.model to javafx.base;
    exports org.example.ordinara;
}
