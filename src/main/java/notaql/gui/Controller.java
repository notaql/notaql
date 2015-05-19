/*
 * Copyright 2015 by Thomas Lottermann
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package notaql.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import notaql.NotaQL;
import notaql.engines.Engine;
import notaql.engines.EngineService;
import org.controlsfx.dialog.Dialogs;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Controller implements Initializable {

    public TextArea queryArea;
    public Button executeButton;
    public ProgressIndicator indicator;
    public GridPane engineGrid;
    public ComboBox<Engine> inEngineComboBox;
    public ComboBox<Engine> outEngineComboBox;

    private List<TextField> inArgumentFields = new LinkedList<>();
    private List<TextField> outArgumentFields = new LinkedList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        inEngineComboBox.setItems(FXCollections.observableArrayList(EngineService.getInstance().getEngines()));
        outEngineComboBox.setItems(FXCollections.observableArrayList(EngineService.getInstance().getEngines()));
    }

    public void execute(ActionEvent actionEvent) {
        engineGrid.setDisable(true);
        queryArea.setDisable(true);
        executeButton.setDisable(true);
        indicator.setVisible(true);
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                evaluate();
                Platform.runLater(() -> {
                    indicator.setVisible(false);
                    executeButton.setDisable(false);
                    queryArea.setDisable(false);
                    engineGrid.setDisable(false);
                });
            }
        };
        new Thread(runnable).start();
    }

    private void evaluate() {
        String expression = this.queryArea.getText();

        if(inEngineComboBox.getSelectionModel().isEmpty() || outEngineComboBox.getSelectionModel().isEmpty()) {
            Platform.runLater(() -> {
                Dialogs.create()
                        .message("Please select input and output engines")
                        .masthead("Incomplete input")
                        .title("Incomplete input")
                        .showError();
            });
            return;
        }

        if(!(expression.contains("IN-ENGINE") || expression.contains("OUT-ENGINE") )) {
            if(inArgumentFields.stream().anyMatch(f -> f.getText().isEmpty()) || outArgumentFields.stream().anyMatch(f -> f.getText().isEmpty())) {
                Platform.runLater(() -> {
                    Dialogs.create()
                            .message("Please provide all properties for the input and output engines")
                            .masthead("Incomplete input")
                            .title("Incomplete input")
                            .showError();
                });
                return;
            }

            String inEngine = "IN-ENGINE:" + inEngineComboBox.getSelectionModel().getSelectedItem().getEngineName() +
                    "(" +
                    inArgumentFields.stream()
                            .map(f -> f.getPromptText() + " <- '" + f.getText() + "'")
                            .collect(Collectors.joining(",")) +
                    ")";
            String outEngine = "OUT-ENGINE:" + outEngineComboBox.getSelectionModel().getSelectedItem().getEngineName() +
                    "(" +
                    outArgumentFields.stream()
                            .map(f -> f.getPromptText() + " <- '" + f.getText() + "'")
                            .collect(Collectors.joining(",")) +
                    ")";

            expression = inEngine + ",\n" + outEngine + ",\n" + expression;
        }

        try {


            NotaQL.evaluate(expression);
        } catch (Exception e) {
            Platform.runLater(() -> {
                Dialogs.create()
                        .masthead("An exception was thrown")
                        .title("Exception")
                        .showException(e);
            });
        }
    }

    public void selectInEngine(ActionEvent actionEvent) {
        selectEngine(inEngineComboBox, inArgumentFields, 0);
    }

    public void selectOutEngine(ActionEvent actionEvent) {
        selectEngine(outEngineComboBox, outArgumentFields, 1);
    }

    private void selectEngine(ComboBox<Engine> engineComboBox, List<TextField> textFields, int col) {
        // remove preious fields
        engineGrid.getChildren().removeAll(textFields);
        textFields.clear();

        // for each argument: create a new textfield
        final List<String> arguments = engineComboBox.getSelectionModel().getSelectedItem().getArguments();

        int i = 1;
        for (String argument : arguments) {
            final TextField argumentField = new TextField();

            argumentField.setPromptText(argument);

            textFields.add(argumentField);
            engineGrid.add(argumentField, col, i);

            i++;
        }
    }
}
