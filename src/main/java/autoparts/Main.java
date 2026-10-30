package autoparts;

import autoparts.view.Navigator;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Clase principal: arranca la aplicación ORIGEN - Portal de Repuestos.
 *
 * Más adelante, aquí se crearán los servicios pasándoles su DAO, por ejemplo:
 * new ComparatorService(new OfferFileDAO()).
 *
 * @author Jahdiel Torres
 */
public class Main extends Application {

    @Override
    public void start(Stage window) {
        Navigator.init(window);
        Navigator.goTo(Navigator.LOGIN);
        window.setTitle("ORIGEN | Portal de Repuestos");
        window.setMinWidth(1000);
        window.setMinHeight(640);
        window.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
