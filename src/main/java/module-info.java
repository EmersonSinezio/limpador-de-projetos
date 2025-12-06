module org.example.limpador_projetos {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.commons.compress;


    opens org.example.limpador_projetos to javafx.fxml;
    exports org.example.limpador_projetos;
}