module IronGateVault {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires java.sql;
    requires java.mail;

    exports com.irongate.ui to javafx.graphics;
    opens com.irongate.ui to javafx.graphics, javafx.fxml;
    opens com.irongate.model to javafx.base;
    opens com.irongate.service to javafx.base;
    opens com.irongate.dao to javafx.base;
    opens com.irongate.util to javafx.base;
    opens com.irongate.security to javafx.base;
}