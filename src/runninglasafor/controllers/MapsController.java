package runninglasafor.controllers;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import upv.ipc.sportlib.MapRegion;
import upv.ipc.sportlib.SportActivityApp;

public class MapsController implements Initializable {

    @FXML private VBox rootBox;
    @FXML private ListView<MapRegion> mapsList;
    @FXML private Label statusLabel;

    private final ObservableList<MapRegion> items = FXCollections.observableArrayList();
    private RootLayoutController root;
    private ResourceBundle bundle;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.bundle = rb;
        mapsList.setItems(items);
        mapsList.setCellFactory(lv -> new MapCell(bundle, this));
        refresh();
    }

    public void setRoot(RootLayoutController root) {
        this.root = root;
    }

    @FXML
    private void onAdd(ActionEvent event) {
        Optional<MapRegion> result = AddMapController.showDialog(mapsList.getScene().getWindow());
        result.ifPresent(r -> refresh());
    }

    private void refresh() {
        List<MapRegion> all = SportActivityApp.getInstance().getMapRegions();
        items.setAll(all == null ? List.of() : all);
        if (statusLabel != null) {
            if (items.isEmpty()) {
                statusLabel.setText(bundle.getString("maps.empty"));
            } else {
                statusLabel.setText(MessageFormat.format(
                        bundle.getString("maps.count"), items.size()));
            }
        }
    }

    void deleteRegion(MapRegion region) {
        if (region == null) return;
        if (!isRegionUnused(region)) {
            Alert err = new Alert(Alert.AlertType.INFORMATION, bundle.getString("maps.delete.error"));
            err.setHeaderText(null);
            err.showAndWait();
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                MessageFormat.format(bundle.getString("maps.delete.content"), region.getName()),
                ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText(bundle.getString("maps.delete.header"));
        Optional<ButtonType> res = confirm.showAndWait();
        if (!res.isPresent() || res.get() != ButtonType.OK) return;

        boolean ok = SportActivityApp.getInstance().removeMapRegion(region);
        if (ok) {
            refresh();
        } else {
            Alert err = new Alert(Alert.AlertType.ERROR, bundle.getString("maps.delete.error"));
            err.setHeaderText(null);
            err.showAndWait();
        }
    }

    private boolean isRegionUnused(MapRegion region) {
        List<MapRegion> unused = SportActivityApp.getInstance().getUnusedMapRegions();
        if (unused == null) return false;
        return unused.stream().anyMatch(r -> r.getId() == region.getId());
    }

    private static final class MapCell extends ListCell<MapRegion> {
        private final ResourceBundle bundle;
        private final MapsController owner;
        private final ImageView thumb = new ImageView();
        private final Label name = new Label();
        private final Label meta = new Label();
        private final Label coords = new Label();
        private final Button deleteBtn = new Button();
        private final HBox container;

        MapCell(ResourceBundle bundle, MapsController owner) {
            this.bundle = bundle;
            this.owner = owner;

            thumb.setFitWidth(80);
            thumb.setFitHeight(50);
            thumb.setPreserveRatio(true);
            thumb.getStyleClass().add("map-row-thumb");

            name.getStyleClass().add("map-row-title");
            meta.getStyleClass().add("map-row-meta");
            coords.getStyleClass().add("map-row-meta");

            VBox info = new VBox(2.0, name, meta, coords);
            info.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(info, Priority.ALWAYS);

            deleteBtn.setText(bundle.getString("maps.delete"));
            deleteBtn.getStyleClass().add("secondary-button");
            deleteBtn.setOnAction(e -> owner.deleteRegion(getItem()));
            deleteBtn.setTooltip(new Tooltip(bundle.getString("maps.delete")));

            container = new HBox(14.0, thumb, info, deleteBtn);
            container.setAlignment(Pos.CENTER_LEFT);
        }

        @Override
        protected void updateItem(MapRegion r, boolean empty) {
            super.updateItem(r, empty);
            if (empty || r == null) {
                setGraphic(null);
                setText(null);
                return;
            }
            name.setText(r.getName());
            meta.setText(MessageFormat.format(bundle.getString("maps.row.region"), r.getName()));
            coords.setText(String.format("Lat %.4f / %.4f  Lon %.4f / %.4f",
                    r.getLatMin(), r.getLatMax(), r.getLonMin(), r.getLonMax()));
            try {
                File f = new File(r.getImagePath());
                if (f.exists()) {
                    thumb.setImage(new Image(f.toURI().toString()));
                } else {
                    thumb.setImage(null);
                }
            } catch (Exception ex) {
                thumb.setImage(null);
            }
            deleteBtn.setDisable(!owner.isRegionUnused(r));
            setGraphic(container);
            setText(null);
        }
    }
}
