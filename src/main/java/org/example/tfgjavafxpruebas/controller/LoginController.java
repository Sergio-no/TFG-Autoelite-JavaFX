package org.example.tfgjavafxpruebas.controller;

import org.example.tfgjavafxpruebas.AutoEliteApp;
import org.example.tfgjavafxpruebas.service.AuthService;
import org.example.tfgjavafxpruebas.sesion.UserSesion;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class LoginController {

    // ══════════════════════════════════════════════════════════
    // MASTER PASSWORD — Cambiar aquí la contraseña maestra
    // ══════════════════════════════════════════════════════════
    private static final String MASTER_PASSWORD = "AutoElite2026";
    private static final int MAX_INTENTOS = 3;
    // ══════════════════════════════════════════════════════════

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisible;
    @FXML private Button togglePasswordBtn;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private ProgressIndicator loadingIndicator;

    private boolean passwordShowing = false;
    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        // Sincronizar campos de contraseña
        passwordVisible.setVisible(false);
        passwordVisible.setManaged(false);
        passwordVisible.textProperty().bindBidirectional(passwordField.textProperty());
    }

    @FXML
    public void togglePasswordVisibility() {
        passwordShowing = !passwordShowing;

        if (passwordShowing) {
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            passwordVisible.setVisible(true);
            passwordVisible.setManaged(true);
            togglePasswordBtn.setText("🙈");
        } else {
            passwordVisible.setVisible(false);
            passwordVisible.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            togglePasswordBtn.setText("👁");
        }
    }

    @FXML
    public void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordShowing
                ? passwordVisible.getText()
                : passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Por favor, introduce email y contraseña.");
            return;
        }

        setLoading(true);

        new Thread(() -> {
            try {
                String token = authService.login(email, password);
                String uid   = authService.getUid(token);
                String rol   = authService.getRolFromBackend(token);

                if ("CLIENTE".equals(rol)) {
                    Platform.runLater(() -> {
                        setLoading(false);
                        showError("Los clientes no pueden acceder al panel de gestión.\n"
                                + "Usa la aplicación móvil.");
                    });
                    return;
                }

                UserSesion.getInstance().setToken(token);
                UserSesion.getInstance().setEmail(email);
                UserSesion.getInstance().setUid(uid);
                UserSesion.getInstance().setRol(rol);

                Platform.runLater(() -> {
                    setLoading(false);
                    AutoEliteApp.navigateTo("dashboard");
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    setLoading(false);
                    showError("Credenciales incorrectas o error de conexión.");
                });
            }
        }).start();
    }

    @FXML
    public void goToRegister() {
        mostrarDialogoMasterPassword();
    }

    // ══════════════════════════════════════════════════════════
    // Diálogo de Master Password con 3 intentos y ojito
    // ══════════════════════════════════════════════════════════
    private void mostrarDialogoMasterPassword() {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Acceso restringido");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // ── Estilos del diálogo ──
        dialog.getDialogPane().setStyle(
                "-fx-background-color: #1a1d23; -fx-min-width: 400;");

        // ── Título ──
        Label titulo = new Label("Contraseña maestra");
        titulo.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 16px; "
                + "-fx-font-weight: bold;");

        Label subtitulo = new Label(
                "Introduce la contraseña de administrador para\n"
                        + "acceder al registro de nuevas cuentas.");
        subtitulo.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
        subtitulo.setWrapText(true);

        // ── Campo de contraseña con ojito ──
        PasswordField masterPassField = new PasswordField();
        masterPassField.setPromptText("Contraseña maestra");
        masterPassField.getStyleClass().add("input-field");
        masterPassField.setStyle(
                "-fx-background-color: #12151a; -fx-text-fill: #e2e8f0; "
                        + "-fx-border-color: #2e333d; -fx-border-width: 1; "
                        + "-fx-border-radius: 6 0 0 6; -fx-background-radius: 6 0 0 6; "
                        + "-fx-padding: 10 14; -fx-font-size: 13px; -fx-pref-height: 40;");

        TextField masterPassVisible = new TextField();
        masterPassVisible.setPromptText("Contraseña maestra");
        masterPassVisible.setStyle(
                "-fx-background-color: #12151a; -fx-text-fill: #e2e8f0; "
                        + "-fx-border-color: #2e333d; -fx-border-width: 1; "
                        + "-fx-border-radius: 6 0 0 6; -fx-background-radius: 6 0 0 6; "
                        + "-fx-padding: 10 14; -fx-font-size: 13px; -fx-pref-height: 40;");
        masterPassVisible.setVisible(false);
        masterPassVisible.setManaged(false);
        masterPassVisible.textProperty().bindBidirectional(masterPassField.textProperty());

        Button toggleBtn = new Button("👁");
        toggleBtn.setStyle(
                "-fx-background-color: #12151a; -fx-text-fill: #6b7280; "
                        + "-fx-border-color: #2e333d; -fx-border-width: 1 1 1 0; "
                        + "-fx-border-radius: 0 6 6 0; -fx-background-radius: 0 6 6 0; "
                        + "-fx-padding: 10 12; -fx-font-size: 14px; -fx-cursor: hand; "
                        + "-fx-pref-height: 40;");

        final boolean[] masterPassShowing = {false};
        toggleBtn.setOnAction(e -> {
            masterPassShowing[0] = !masterPassShowing[0];
            if (masterPassShowing[0]) {
                masterPassField.setVisible(false);
                masterPassField.setManaged(false);
                masterPassVisible.setVisible(true);
                masterPassVisible.setManaged(true);
                toggleBtn.setText("🙈");
            } else {
                masterPassVisible.setVisible(false);
                masterPassVisible.setManaged(false);
                masterPassField.setVisible(true);
                masterPassField.setManaged(true);
                toggleBtn.setText("👁");
            }
        });

        HBox passBox = new HBox(0, masterPassField, masterPassVisible, toggleBtn);
        passBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(masterPassField, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(masterPassVisible, javafx.scene.layout.Priority.ALWAYS);

        // ── Label de error e intentos ──
        Label errorMaster = new Label("");
        errorMaster.setStyle("-fx-text-fill: #f87171; -fx-font-size: 12px;");
        errorMaster.setWrapText(true);

        Label intentosLabel = new Label("Intentos restantes: " + MAX_INTENTOS);
        intentosLabel.setStyle("-fx-text-fill: #d4a72c; -fx-font-size: 11px;");

        // ── Layout ──
        VBox content = new VBox(12,
                titulo, subtitulo,
                new Separator(),
                new Label("Contraseña *") {{
                    setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
                }},
                passBox,
                errorMaster,
                intentosLabel
        );
        content.setStyle("-fx-padding: 20;");
        content.setPrefWidth(380);
        dialog.getDialogPane().setContent(content);

        // ── Separador del diálogo con estilo ──
        try {
            dialog.getDialogPane().lookup(".separator").setStyle(
                    "-fx-background-color: #2e333d;");
        } catch (Exception ignored) {}

        // ── Control de intentos ──
        final int[] intentosRestantes = {MAX_INTENTOS};

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                String input = masterPassField.getText();

                if (MASTER_PASSWORD.equals(input)) {
                    return true;
                } else {
                    intentosRestantes[0]--;

                    if (intentosRestantes[0] <= 0) {
                        errorMaster.setText("Has agotado los 3 intentos.");
                        intentosLabel.setText("Sin intentos restantes");
                        intentosLabel.setStyle(
                                "-fx-text-fill: #f87171; -fx-font-size: 11px;");

                        // Cerrar y mostrar error final
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Acceso denegado");
                            alert.setHeaderText(null);
                            alert.setContentText(
                                    "Has agotado los 3 intentos para introducir\n"
                                            + "la contraseña maestra. Contacta con el\n"
                                            + "administrador del sistema.");
                            alert.getDialogPane().setStyle(
                                    "-fx-background-color: #1a1d23;");
                            try {
                                alert.getDialogPane().lookup(".content.label")
                                        .setStyle("-fx-text-fill: #e2e8f0;");
                            } catch (Exception ignored2) {}
                            alert.showAndWait();
                        });
                        return false;
                    }

                    // Aún quedan intentos → mostrar error y reabrir
                    errorMaster.setText("Contraseña incorrecta.");
                    intentosLabel.setText(
                            "Intentos restantes: " + intentosRestantes[0]);

                    // Limpiar campo
                    masterPassField.clear();

                    // Reabrir el diálogo con los intentos que quedan
                    Platform.runLater(() ->
                            mostrarDialogoMasterPasswordConIntentos(
                                    intentosRestantes[0]));
                    return false;
                }
            }
            return false;
        });

        dialog.showAndWait().ifPresent(ok -> {
            if (Boolean.TRUE.equals(ok)) {
                AutoEliteApp.navigateTo("register");
            }
        });
    }

    /**
     * Versión del diálogo que ya tiene intentos consumidos.
     * Se reabre tras un fallo, mostrando los intentos restantes.
     */
    private void mostrarDialogoMasterPasswordConIntentos(int intentos) {
        if (intentos <= 0) return;

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Acceso restringido");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle(
                "-fx-background-color: #1a1d23; -fx-min-width: 400;");

        Label titulo = new Label("Contraseña maestra");
        titulo.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 16px; "
                + "-fx-font-weight: bold;");

        Label avisoError = new Label("Contraseña incorrecta. Inténtalo de nuevo.");
        avisoError.setStyle("-fx-text-fill: #f87171; -fx-font-size: 12px;");

        // ── Campo con ojito ──
        PasswordField masterPassField = new PasswordField();
        masterPassField.setPromptText("Contraseña maestra");
        masterPassField.setStyle(
                "-fx-background-color: #12151a; -fx-text-fill: #e2e8f0; "
                        + "-fx-border-color: #2e333d; -fx-border-width: 1; "
                        + "-fx-border-radius: 6 0 0 6; -fx-background-radius: 6 0 0 6; "
                        + "-fx-padding: 10 14; -fx-font-size: 13px; -fx-pref-height: 40;");

        TextField masterPassVisible = new TextField();
        masterPassVisible.setPromptText("Contraseña maestra");
        masterPassVisible.setStyle(
                "-fx-background-color: #12151a; -fx-text-fill: #e2e8f0; "
                        + "-fx-border-color: #2e333d; -fx-border-width: 1; "
                        + "-fx-border-radius: 6 0 0 6; -fx-background-radius: 6 0 0 6; "
                        + "-fx-padding: 10 14; -fx-font-size: 13px; -fx-pref-height: 40;");
        masterPassVisible.setVisible(false);
        masterPassVisible.setManaged(false);
        masterPassVisible.textProperty().bindBidirectional(masterPassField.textProperty());

        Button toggleBtn = new Button("👁");
        toggleBtn.setStyle(
                "-fx-background-color: #12151a; -fx-text-fill: #6b7280; "
                        + "-fx-border-color: #2e333d; -fx-border-width: 1 1 1 0; "
                        + "-fx-border-radius: 0 6 6 0; -fx-background-radius: 0 6 6 0; "
                        + "-fx-padding: 10 12; -fx-font-size: 14px; -fx-cursor: hand; "
                        + "-fx-pref-height: 40;");

        final boolean[] showing = {false};
        toggleBtn.setOnAction(e -> {
            showing[0] = !showing[0];
            if (showing[0]) {
                masterPassField.setVisible(false);
                masterPassField.setManaged(false);
                masterPassVisible.setVisible(true);
                masterPassVisible.setManaged(true);
                toggleBtn.setText("🙈");
            } else {
                masterPassVisible.setVisible(false);
                masterPassVisible.setManaged(false);
                masterPassField.setVisible(true);
                masterPassField.setManaged(true);
                toggleBtn.setText("👁");
            }
        });

        HBox passBox = new HBox(0, masterPassField, masterPassVisible, toggleBtn);
        passBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(masterPassField, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(masterPassVisible, javafx.scene.layout.Priority.ALWAYS);

        Label errorLabel2 = new Label("");
        errorLabel2.setStyle("-fx-text-fill: #f87171; -fx-font-size: 12px;");

        Label intentosLabel = new Label("Intentos restantes: " + intentos);
        intentosLabel.setStyle("-fx-text-fill: #d4a72c; -fx-font-size: 11px;");

        VBox content = new VBox(12,
                titulo, avisoError,
                new Separator(),
                new Label("Contraseña *") {{
                    setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
                }},
                passBox,
                errorLabel2,
                intentosLabel
        );
        content.setStyle("-fx-padding: 20;");
        content.setPrefWidth(380);
        dialog.getDialogPane().setContent(content);

        final int[] restantes = {intentos};

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                String input = masterPassField.getText();

                if (MASTER_PASSWORD.equals(input)) {
                    return true;
                } else {
                    restantes[0]--;

                    if (restantes[0] <= 0) {
                        Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Acceso denegado");
                            alert.setHeaderText(null);
                            alert.setContentText(
                                    "Has agotado los 3 intentos para introducir\n"
                                            + "la contraseña maestra. Contacta con el\n"
                                            + "administrador del sistema.");
                            alert.getDialogPane().setStyle(
                                    "-fx-background-color: #1a1d23;");
                            try {
                                alert.getDialogPane().lookup(".content.label")
                                        .setStyle("-fx-text-fill: #e2e8f0;");
                            } catch (Exception ignored) {}
                            alert.showAndWait();
                        });
                        return false;
                    }

                    masterPassField.clear();
                    Platform.runLater(() ->
                            mostrarDialogoMasterPasswordConIntentos(
                                    restantes[0]));
                    return false;
                }
            }
            return false;
        });

        dialog.showAndWait().ifPresent(ok -> {
            if (Boolean.TRUE.equals(ok)) {
                AutoEliteApp.navigateTo("register");
            }
        });
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    private void setLoading(boolean loading) {
        loadingIndicator.setVisible(loading);
        loginButton.setDisable(loading);
        loginButton.setText(loading ? "Entrando..." : "Entrar");
    }
}
