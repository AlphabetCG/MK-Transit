package mktransit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class GuiTest extends Application {

    private double scale = 1.5;
    private final double minScale = 1.65;
    private final double maxScale = 7;

    @Override
    public void start(Stage stage) {

        JsonReader reader = new JsonReader();
        reader.loadJsonData(); // แค่โหลด

        List<Line> lines = reader.getLines(); // ดึงข้อมูล Line
        Map<String, Station> stationMap = reader.getStationMap(); // ดึงข้อมูล Station

        PathFinder pathFinder = new PathFinder(stationMap);

        // โหลดสถานีมาจาก JsonReader
        List<Station> stationList = new ArrayList<>(reader.getStationMap().values());
        StationUtil stationUtil = new StationUtil(stationList);

        HBox root = new HBox();

        Scene scene = new Scene(root, 1530, 790);

        // ---------- LEFT ----------
        StackPane leftPane = new StackPane();
        leftPane.setPrefWidth(150);

        // Map image
        Image image = new Image("https://www.bts.co.th/assets/images/yellow-map.jpg");
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.fitWidthProperty().bind(leftPane.widthProperty().multiply(0.5)); // ปรับขนาดเริ่มต้นอิงขนาด pane
        imageView.setStyle(
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.5, 0, 5); -fx-background-radius: 10;");

        // Group ที่ใช้ scale/translate
        Group zoomGroup = new Group(imageView);

        // ตั้งค่าขนาดเริ่มต้นของ zoomGroup ให้เท่ากับ minScale
        zoomGroup.setScaleX(minScale);
        zoomGroup.setScaleY(minScale);
        scale = minScale; // กำหนดค่า scale เริ่มต้นให้เท่ากับ minScale

        // Clip กำหนดขอบไม่ให้ภาพหลุด
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(leftPane.widthProperty());
        clip.heightProperty().bind(leftPane.heightProperty());
        leftPane.setClip(clip);

        final double[] mouseAnchorX = new double[1];
        final double[] mouseAnchorY = new double[1];
        final double[] translateAnchorX = new double[1];
        final double[] translateAnchorY = new double[1];

        // Handle dragging
        zoomGroup.setOnMousePressed(event -> {
            mouseAnchorX[0] = event.getSceneX();
            mouseAnchorY[0] = event.getSceneY();
            translateAnchorX[0] = zoomGroup.getTranslateX();
            translateAnchorY[0] = zoomGroup.getTranslateY();
        });

        zoomGroup.setOnMouseDragged(event -> {
            if (scale > 5.0) {
                double deltaX = event.getSceneX() - mouseAnchorX[0];
                double deltaY = event.getSceneY() - mouseAnchorY[0];
                zoomGroup.setTranslateX(translateAnchorX[0] + deltaX);
                zoomGroup.setTranslateY(translateAnchorY[0] + deltaY);
            }
        });

        // ซูมเฉพาะเมื่อเมาส์อยู่บนรูป

        imageView.setOnScroll((ScrollEvent event) -> {
            double zoomFactor = 1.1;
            double deltaY = event.getDeltaY();

            double oldScale = scale;
            if (deltaY < 0) {
                // ซูมออก: fix กลับศูนย์กลาง
                scale /= zoomFactor;
            } else {
                // ซูมเข้า: ซูมตามเมาส์
                scale *= zoomFactor;
            }

            scale = Math.max(minScale, Math.min(scale, maxScale));
            double factor = scale / oldScale;

            zoomGroup.setScaleX(scale);
            zoomGroup.setScaleY(scale);

            if (deltaY < 0) {
                // ซูมออก: reset กลับศูนย์กลาง
                zoomGroup.setTranslateX(0);
                zoomGroup.setTranslateY(0);
            } else {
                // ซูมเข้า: zoom ตามเมาส์
                Bounds bounds = zoomGroup.localToScene(zoomGroup.getBoundsInLocal());
                double dx = event.getSceneX() - (bounds.getMinX() + bounds.getWidth() / 2);
                double dy = event.getSceneY() - (bounds.getMinY() + bounds.getHeight() / 2);

                zoomGroup.setTranslateX(zoomGroup.getTranslateX() - (factor - 1) * dx);
                zoomGroup.setTranslateY(zoomGroup.getTranslateY() - (factor - 1) * dy);
            }

            event.consume();
        });

        leftPane.getChildren().add(zoomGroup);

        // ---------- RIGHT ----------
        VBox rightPane = new VBox(20);
        rightPane.setPrefWidth(750);
        rightPane.setStyle("-fx-padding: 50 100 20 100; -fx-alignment: center;"); // Top Right Bottom Left ,Padding
                                                                                  // และจัดให้อยู่ตรงกลาง

        // Logo
        Image logoImage = new Image(
                "https://th.m.wikipedia.org/wiki/%E0%B9%84%E0%B8%9F%E0%B8%A5%E0%B9%8C:BTS-Logo_Gold.svg"); // URL
                                                                                                           // ของโลโก้
        ImageView logoView = new ImageView(logoImage);
        logoView.setFitWidth(100); // กำหนดขนาดโลโก้
        logoView.setPreserveRatio(true);

        // Group for Project Name and TextFields
        VBox contentBox = new VBox(15); // ระยะห่างระหว่างองค์ประกอบในกรอบ
        contentBox.setStyle(
                "-fx-border-width: 2; -fx-padding: 0 0 25 0 ; -fx-background-color: #f9f9f9;-fx-alignment: center; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.5, 0, 5); -fx-background-radius: 10;"); // กำหนดกรอบและพื้นหลัง

        StackPane bgName = new StackPane();
        bgName.setStyle("-fx-background-color: #003366; -fx-padding: 10;"); // กำหนดกรอบและพื้นหลัง

        // Project Name
        Label projectName = new Label("MK Transit");
        projectName.setStyle("-fx-text-fill: white; -fx-font-size: 50px; -fx-font-weight: bold;");

        // TextField1
        Label inputLabel1 = new Label("Enter Start Station ID:");
        inputLabel1.setStyle("-fx-text-fill: #003366;-fx-font-weight: bold;-fx-font-size: 13px;");
        TextField textField1 = new TextField();
        textField1.setPromptText("Ex. N24");
        textField1.setMaxWidth(60);

        // Label สำหรับแสดงชื่อสถานี
        Label stationName1 = new Label();
        stationName1.setStyle("-fx-text-fill: #003366; -fx-font-size: 13px; -fx-font-style: italic;");

        Circle circleStation1 = new Circle(6);

        // จัด TextField และ Label ในแนวนอน
        HBox textField1Box = new HBox(10); // ระยะห่างระหว่าง TextField และ Label
        textField1Box.setStyle("-fx-alignment: center;"); // จัดให้อยู่ชิดซ้าย
        textField1Box.getChildren().addAll(textField1);

        // เพิ่ม Listener ให้ TextField1
        textField1.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                stationName1.setText("");
                textField1.setStyle(""); // ล้างชื่อสถานีหากไม่มีการป้อนข้อมูล
                // textField1Box.getChildren().addAll(textField1, circleStation1, stationName1);
                textField1Box.getChildren().remove(stationName1);
                return;
            }

            String stationId = newValue.toUpperCase();
            Station someStation = stationMap.get(stationId); // ดึงข้อมูลสถานีจาก map

            if (!textField1Box.getChildren().contains(stationName1)) {
                textField1Box.getChildren().add(1, stationName1);
            }

            if (someStation == null) {
                stationName1.setText("Station not found"); // แสดงข้อความเมื่อไม่พบสถานี
                textField1.setStyle("-fx-border-color: red;"); // เปลี่ยนสีขอบ TextField เป็นสีแดง
                textField1Box.getChildren().remove(circleStation1);
            } else {
                stationName1.setText(someStation.getName()); // แสดงชื่อสถานี
                textField1.setStyle(""); // ล้างสีขอบ TextField

                if (!textField1Box.getChildren().contains(circleStation1)) {
                    textField1Box.getChildren().add(1, circleStation1); // เพิ่มวงกลมกลับมาในตำแหน่งที่ 2
                }

                switch (someStation.getColor()) {
                    case "blue":
                        circleStation1.setStyle("-fx-fill: #063b82;"); // สีแดง
                        break;
                    case "lightgreen":
                        circleStation1.setStyle("-fx-fill: #84c469;"); // สีเขียวอ่อน
                        break;
                    case "darkgreen":
                        circleStation1.setStyle("-fx-fill: #328674;"); // สีเขียวเข้ม
                        break;
                    case "yellow":
                        circleStation1.setStyle("-fx-fill: #edd240;"); // สีเหลือง
                        break;
                    case "purple":
                        circleStation1.setStyle("-fx-fill: #854289;"); // สีม่วง
                        break;
                    case "darkred":
                        circleStation1.setStyle("-fx-fill: #690606;"); // สีแดงเข้ม
                        break;
                    case "pink":
                        circleStation1.setStyle("-fx-fill: #cb4e9b;"); // สีชมพู
                        break;
                    case "red":
                        circleStation1.setStyle("-fx-fill: #cd6060;"); // สีแดง
                        break;
                    case "gold":
                        circleStation1.setStyle("-fx-fill: #ad9f51;"); // สีทอง
                        break;
                    default:
                        circleStation1.setStyle("-fx-fill: transparent;"); // ซ่อนวงกลมสีหากไม่มีสีที่ตรงกัน
                        break;
                }
            }
        });

        // TextField2
        Label inputLabel2 = new Label("Enter End Station ID:");
        inputLabel2.setStyle("-fx-text-fill: #003366;-fx-font-weight: bold;-fx-font-size: 13px;");
        TextField textField2 = new TextField();
        textField2.setPromptText("Ex. N24");
        textField2.setMaxWidth(60);

        // Label สำหรับแสดงชื่อสถานี
        Label stationName2 = new Label();
        stationName1.setStyle("-fx-text-fill: #003366; -fx-font-size: 13px; -fx-font-style: italic;");

        Circle circleStation2 = new Circle(6);

        // จัด TextField และ Label ในแนวนอน
        HBox textField2Box = new HBox(10); // ระยะห่างระหว่าง TextField และ Label
        textField2Box.setStyle("-fx-alignment: center;"); // จัดให้อยู่ชิดซ้าย
        textField2Box.getChildren().addAll(textField2);

        // เพิ่ม Listener ให้ TextField1
        textField2.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                stationName2.setText("");
                textField2.setStyle(""); // ล้างชื่อสถานีหากไม่มีการป้อนข้อมูล
                textField1Box.getChildren().remove(stationName2);
                return;
            }

            String stationId = newValue.toUpperCase();
            Station someStation = stationMap.get(stationId); // ดึงข้อมูลสถานีจาก map

            if (!textField2Box.getChildren().contains(stationName2)) {
                textField2Box.getChildren().add(1, stationName2);
            }

            if (someStation == null) {
                stationName2.setText("Station not found"); // แสดงข้อความเมื่อไม่พบสถานี
                textField2.setStyle("-fx-border-color: red;"); // เปลี่ยนสีขอบ TextField เป็นสีแดง
                textField2Box.getChildren().remove(circleStation2);
            } else {
                stationName2.setText(someStation.getName()); // แสดงชื่อสถานี
                textField2.setStyle(""); // ล้างสีขอบ TextField

                if (!textField2Box.getChildren().contains(circleStation2)) {
                    textField2Box.getChildren().add(1, circleStation2); // เพิ่มวงกลมกลับมาในตำแหน่งที่ 2
                }

                switch (someStation.getColor()) {
                    case "blue":
                        circleStation2.setStyle("-fx-fill: #063b82;"); // สีแดง
                        break;
                    case "lightgreen":
                        circleStation2.setStyle("-fx-fill: #84c469;"); // สีเขียวอ่อน
                        break;
                    case "darkgreen":
                        circleStation2.setStyle("-fx-fill: #328674;"); // สีเขียวเข้ม
                        break;
                    case "yellow":
                        circleStation2.setStyle("-fx-fill: #edd240;"); // สีเหลือง
                        break;
                    case "purple":
                        circleStation2.setStyle("-fx-fill: #854289;"); // สีม่วง
                        break;
                    case "darkred":
                        circleStation2.setStyle("-fx-fill: #690606;"); // สีแดงเข้ม
                        break;
                    case "pink":
                        circleStation2.setStyle("-fx-fill: #cb4e9b;"); // สีชมพู
                        break;
                    case "red":
                        circleStation2.setStyle("-fx-fill: #cd6060;"); // สีแดง
                        break;
                    case "gold":
                        circleStation2.setStyle("-fx-fill: #ad9f51;"); // สีทอง
                        break;
                    default:
                        circleStation2.setStyle("-fx-fill: transparent;"); // ซ่อนวงกลมสีหากไม่มีสีที่ตรงกัน
                        break;
                }
            }
        });

        bgName.getChildren().addAll(projectName);

        // วงกลม 3 อัน
        VBox circleBox = new VBox(4); // ระยะห่างระหว่างวงกลม
        circleBox.setStyle("-fx-alignment: center;"); // จัดให้อยู่ตรงกลาง

        Circle circle1 = new Circle(5); // วงกลมขนาดรัศมี 10
        circle1.setStyle("-fx-fill: #003366;"); // สีแดง

        Circle circle2 = new Circle(5); // วงกลมขนาดรัศมี 10
        circle2.setStyle("-fx-fill: #003366;"); // สีเขียว

        Circle circle3 = new Circle(5); // วงกลมขนาดรัศมี 10
        circle3.setStyle("-fx-fill: #003366;"); // สีน้ำเงิน

        // เพิ่มวงกลมเข้าไปใน HBox
        circleBox.getChildren().addAll(circle1, circle2, circle3);

        // Button
        Button submitButton = new Button("Submit");
        submitButton.setStyle(
                "-fx-background-color: #003366; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15; -fx-border-radius: 5; -fx-background-radius: 5;");

        Button clearButton = new Button("Clear");
        clearButton.setStyle(
                "-fx-background-color:rgb(196, 0, 0); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 15; -fx-border-radius: 5; -fx-background-radius: 5;");

        // Add action for buttons
        submitButton.setOnAction(event -> {
            String startId = textField1.getText().trim(); // ดึงค่า Start Station ID
            String endId = textField2.getText().trim(); // ดึงค่า End Station ID

            if (startId.isEmpty() || endId.isEmpty()) {
                System.out.println("กรุณากรอกข้อมูลให้ครบถ้วน!"); // แสดงข้อความเมื่อไม่มีการป้อนข้อมูล
                return;
            }

            // สร้างหน้าถัดไป
            VBox nextPage = new VBox(20);
            nextPage.setStyle("-fx-alignment: center; -fx-padding: 20;");
            nextPage.setPrefSize(1530, 790); // ขนาดเริ่มต้น

            // Logo
            ImageView logoView1 = new ImageView(logoImage);
            logoView1.setFitWidth(100); // กำหนดขนาดโลโก้
            logoView1.setPreserveRatio(true);

            // Project Name
            Label projectName1 = new Label("MK Transit");
            projectName1.setStyle("-fx-text-fill: #003366; -fx-font-size: 50px; -fx-font-weight: bold;");

            VBox PathBox1 = new VBox(10); // ใช้ VBox ที่ถูกต้อง
            PathBox1.setStyle("-fx-alignment: center; -fx-padding: 20; -fx-border-width: 2; -fx-padding: 0 0 25 0; "
                    + "-fx-background-color: #f9f9f9; -fx-alignment: center; "
                    + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.5, 0, 5); -fx-background-radius: 10;");
            PathBox1.setMaxWidth(700);

            Label infoLabel = new Label("Travel Information:");
            infoLabel.setStyle(
                    "-fx-text-fill: #003366; -fx-font-size: 20px; -fx-font-weight: bold; -fx-alignment: left;");

            Label startIdLabel;
            Label endIdLabel;

            // สร้าง Label สำหรับ PathBox1
            Label pathLabel1 = new Label("เส้นทาง: สถานี A -> สถานี B -> สถานี C");
            pathLabel1.setStyle("-fx-text-fill: #003366; -fx-font-size: 14px; -fx-font-weight: bold;");

            Label durationLabel1 = new Label("ระยะเวลา: 30 นาที");
            durationLabel1.setStyle("-fx-text-fill: #003366; -fx-font-size: 14px;");

            Label priceLabel1 = new Label("ราคา: 45 บาท");
            priceLabel1.setStyle("-fx-text-fill: #003366; -fx-font-size: 14px;");

            VBox infoBox = new VBox(10); // ใช้ VBox ที่ถูกต้อง
            infoBox.setStyle("-fx-alignment: left; -fx-padding: 20; -fx-border-width: 2; -fx-padding: 0 0 25 0; "
                    + "-fx-background-color: #f9f9f9; -fx-alignment: left; "
                    + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.5, 0, 5); -fx-background-radius: 10;");

            Button backButton = new Button("กลับไปหน้าหลัก");
            backButton.setStyle("-fx-background-color: #003366; -fx-text-fill: white; -fx-font-weight: bold;");
            VBox.setVgrow(backButton, Priority.ALWAYS); // อนุญาตให้ปุ่มขยายตัวตาม VBox

            // กดปุ่ม Back เพื่อกลับไปหน้าหลัก
            backButton.setOnAction(e -> {
                stage.setScene(scene); // กลับไปยัง Scene หลัก
            });

            nextPage.getChildren().addAll(logoView1, projectName1, PathBox1, backButton);

            PathResult result = pathFinder.findShortestPath(startId, endId);
            int i = 0;

            if (result.getFullPath().isEmpty()) {
                System.out
                        .println("❌ ไม่พบเส้นทางจาก " + stationUtil.IDtoName(startId) + "(" + startId + ")" + " ไปยัง "
                                + stationUtil.IDtoName(endId) + "(" + endId + ")");
            } else {
                System.out.println("✅ เจอเส้นทาง!");
                System.out.println("เส้นทางเดินทั้งหมด:");

                for (String stationId : result.getFullPath()) {
                Station station = stationMap.get(stationId);
                System.out.println("- " + station.getName() + " (" + station.getId() + ")");
                i++;
                }

                List<String> fullPath = result.getFullPath();
                List<String> importantSteps = PathUtil.filterImportantStepsWithActualTransfers(fullPath, stationMap);

                if (importantSteps.isEmpty()) {
                    System.out.print("📍 ไม่มีจุดที่ต้องเปลี่ยนสายตลอดเส้นทาง");
                    System.out.println(
                            stationUtil.IDtoName(startId) + " (" + startId + ") ➜ " + stationUtil.IDtoName(endId)
                                    + " (" + endId + ")");

                    startIdLabel = new Label(
                            "Start Station: " + stationUtil.IDtoName(startId) + " (" + startId + ")");
                    startIdLabel.setStyle("-fx-text-fill: #003366; -fx-font-size: 15px; -fx-alignment: left;");
                    endIdLabel = new Label(
                            "End Station: " + stationUtil.IDtoName(endId) + " (" + endId + ")");
                    endIdLabel.setStyle("-fx-text-fill: #003366; -fx-font-size: 15px; -fx-alignment: left;");

                    // เพิ่มองค์ประกอบใน PathBox1
                    PathBox1.getChildren().addAll(infoLabel, startIdLabel, endIdLabel);
                } else {
                    System.out.println("📍 เส้นทางนี้มีการเปลี่ยนสาย | จำนวน " + i + " สถานี");

                    int k = 0;
                    boolean firstStep = true;
                    for (int j = 0; j < importantSteps.size(); j++) {
                        String step = importantSteps.get(j);
                        String[] parts = step.split("->");
                        String fromId = parts[0];
                        String toId = parts[1];

                        String fromName = stationUtil.IDtoName(fromId);
                        String toName = stationUtil.IDtoName(toId);

                        if (!step.equals(startId) && k == 0) {
                            System.out.print("🔄 " + stationUtil.IDtoName(startId) + " (" + startId + ") ➜ ");
                            k++;
                        }

                        if (firstStep) {
                            // เริ่มต้นจากสถานีต้นทางไปยังจุดเปลี่ยนสายแรก
                            System.out.print(fromName + " (" + fromId + ") ➜ " + toName + " (" + toId + ")");
                            firstStep = false;
                        } else {
                            // แสดงเฉพาะจุดเปลี่ยนสายถัดไป
                            System.out.print(" ➜ " + fromName + " (" + fromId + ") ➜ " + toName + " (" + toId + ")");
                        }
                    }

                    // จบด้วยปลายทางถ้ายังไม่ได้แสดง
                    String lastToId = importantSteps.get(importantSteps.size() - 1).split("->")[1];
                    if (!lastToId.equals(endId)) {
                        System.out.print(" ➜ " + stationUtil.IDtoName(endId) + " (" + endId + ")");
                    }

                    System.out.println(); // ขึ้นบรรทัดใหม่
                }

                System.out.println("\n🕒 เวลารวมทั้งหมด: " + result.getTotalTime() + " นาที");
            }

            // สร้าง Scene ใหม่สำหรับหน้าถัดไป
            Scene nextScene = new Scene(nextPage);
            stage.setScene(nextScene); // เปลี่ยน Scene ไปยังหน้าถัดไป
            stage.setResizable(true); // อนุญาตให้ปรับขนาดหน้าต่างได้
        });

        clearButton.setOnAction(event -> {
            textField1.clear();
            textField2.clear();
            textField1Box.getChildren().remove(circleStation1);
            textField2Box.getChildren().remove(circleStation2);
            textField1Box.getChildren().remove(stationName1);
            textField2Box.getChildren().remove(stationName2);
        });

        // Add buttons to an HBox
        HBox buttonBox = new HBox(10); // ระยะห่างระหว่างปุ่ม
        buttonBox.setStyle("-fx-alignment: center;"); // จัดให้อยู่ตรงกลาง
        buttonBox.getChildren().addAll(submitButton, clearButton);

        // Add elements to the contentBox
        contentBox.getChildren().addAll(bgName, inputLabel1, textField1Box, circleBox, inputLabel2,
                textField2Box, buttonBox);

        // Add elements to the rightPane
        rightPane.getChildren().addAll(logoView, contentBox);

        // ---------- Layout ----------
        HBox.setHgrow(leftPane, Priority.ALWAYS);
        root.getChildren().addAll(leftPane, rightPane);

        stage.setTitle("MK Transit");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}