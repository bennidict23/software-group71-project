module com.example {  // 替换为你的模块名（如 com.example）
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jfree.jfreechart; // 尝试可能的模块名
    requires json.simple; // 同理，json-simple-1.1.1.jar 的模块名是 json.simple
    requires java.net.http;
    requires juniversalchardet;

    opens org.example.list to javafx.base;
    opens org.example to javafx.graphics, javafx.fxml; // 允许javafx.graphics访问org.example包
    exports org.example; // 如果需要跨模块调用
}