# 分析模块集成指南

本文档提供了如何将财务分析模块与其他模块集成的指导。

## 模块结构

财务分析模块遵循MVC (Model-View-Controller) 架构模式：

- **边界类 (Boundary/View)**：位于 `com.financetracker.boundary.analysis` 包中
  - `AnalysisView`：主分析视图容器
  - `SpendingStructureView`：支出结构分析（饼图）
  - `SpendingForecastView`：支出预测分析（待完善）
  - `BudgetRecommendationView`：预算推荐（待完善）

- **控制类 (Controller)**：位于 `com.financetracker.control` 包中
  - `DataAnalysisController`：处理数据分析逻辑
  - `DataAccessInterface`：数据访问接口
  - `MockDataAccess`：模拟数据实现（仅用于测试）

- **实体类 (Entity/Model)**：位于 `com.financetracker.entity` 包中
  - `Transaction`：交易记录实体
  - `CategoryExpense`：分类支出实体

## 集成步骤

### 1. 数据访问层集成

分析模块通过 `DataAccessInterface` 接口获取数据。团队成员负责数据访问层需要：

1. 实现 `DataAccessInterface` 接口，创建真实的数据访问对象
2. 修改 `DataAnalysisController` 的默认实例创建方法，使用真实数据源而非模拟数据

```java
// 示例：
public class RealDataAccess implements DataAccessInterface {
    // 实现所有接口方法
    // ...
}

// 在适当的地方修改控制器工厂方法
public static DataAnalysisController getDefaultInstance() {
    return new DataAnalysisController(new RealDataAccess());
}
```

### 2. UI集成

主应用程序已经将分析模块作为标签页整合到 `MainView` 中：

```java
Tab analysisTab = new Tab("Analysis");
analysisTab.setClosable(false);
analysisTab.setContent(new AnalysisView());
```

### 3. 文件导入功能

交易数据导入功能应实现在 `DataAccessInterface` 的 `loadTransactionsFromFile` 方法中。它应支持：

- 不同文件格式（CSV、Excel等）
- 不同来源的交易记录（微信、支付宝、银行等）

## 测试

在集成过程中，可以使用以下方法测试分析模块：

1. 使用 `MockDataAccess` 进行独立测试
2. 编写单元测试验证数据处理逻辑
3. 进行用户界面测试，确保分析图表正确显示数据

## 注意事项

- 分析模块的设计考虑了未来扩展，可以轻松添加新的分析视图
- 确保数据访问层实现线程安全，特别是在文件导入操作期间
- 所有UI更新都应在JavaFX应用线程中进行，避免线程冲突

## 联系方式

如有问题或需要协助集成，请联系分析模块负责人。 