package autoparts.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Ventana "Regístrate para comprar" que se abre cuando un invitado intenta
 * hacer algo que necesita cuenta, como agregar un repuesto al carrito.
 *
 * Tiene dos botones: Registrarme (cierra la sesión de invitado y abre el
 * registro) y Seguir explorando (solo cierra la ventana).
 *
 * @author Héctor Guevara
 */
public class GuestDialog {

    private GuestDialog() {
    }

    /**
     * Abre la ventana encima de la pantalla actual y espera a que se cierre.
     * Si el invitado elige Registrarme, después de cerrar abre el registro.
     *
     * @param anyNode cualquier componente de la pantalla que la abre; sirve
     *                para saber cuál es la ventana de atrás
     */
    public static void show(Node anyNode) {
        Label title = new Label("Regístrate para comprar");
        title.getStyleClass().add("card-title");
        Label message = new Label("Estás explorando como invitado. Crea tu cuenta gratis "
                + "para agregar repuestos al carrito y hacer pedidos.");
        message.getStyleClass().add("secondary-text");
        message.setWrapText(true);

        boolean[] wantsToRegister = {false};
        Stage dialog = new Stage();

        Button register = new Button("Registrarme");
        register.getStyleClass().add("primary-button");
        register.setDefaultButton(true);
        register.setOnAction(event -> {
            wantsToRegister[0] = true;
            dialog.close();
        });

        Button keepExploring = new Button("Seguir explorando");
        keepExploring.getStyleClass().add("secondary-button");
        keepExploring.setCancelButton(true); // Esc también la cierra
        keepExploring.setOnAction(event -> dialog.close());

        HBox buttons = new HBox(10, keepExploring, register);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(12, title, message, buttons);
        root.getStyleClass().add("app-background");
        root.setPadding(new Insets(24));
        root.setPrefWidth(400);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(GuestDialog.class.getResource("styles.css").toExternalForm());

        Window owner = anyNode.getScene().getWindow();
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL); // bloquea la ventana de atrás
        dialog.setTitle("Regístrate para comprar");
        dialog.setResizable(false);
        dialog.setScene(scene);
        dialog.showAndWait();

        if (wantsToRegister[0]) {
            DashboardController.openRegister();
        }
    }
}
