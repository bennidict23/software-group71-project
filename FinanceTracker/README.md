# Finance Tracker - 分析模块

本模块是Finance Tracker应用程序的分析组件，负责提供数据可视化和财务分析功能。

## 项目结构

项目采用MVC架构模式组织：

```
src/main/java/com/financetracker/
├── boundary/            # 边界类（视图）
│   └── analysis/        # 分析相关的视图组件
│       ├── AnalysisView.java              # 主分析视图
│       ├── SpendingStructureView.java     # 支出结构分析（饼图）
│       ├── SpendingForecastView.java      # 支出预测分析（折线图）
│       └── BudgetRecommendationView.java  # 预算推荐（待完善）
├── control/             # 控制器类
│   ├── DataAnalysisController.java        # 数据分析控制器
│   ├── DataAccessInterface.java           # 数据访问接口
│   └── MockDataAccess.java                # 模拟数据实现（测试用）
└── entity/              # 实体类
    ├── Transaction.java                   # 交易记录实体
    └── CategoryExpense.java               # 分类支出实体
```

## 已实现功能

1. **支出结构分析**
   - 基于类别的饼图可视化
   - 可选择不同时间范围（上月、过去3个月、过去6个月、本年度）
   - 分类比例和具体金额显示

2. **支出预测分析**
   - 历史支出趋势图
   - 基于历史数据的未来支出预测
   - 可调整历史数据和预测时间范围

## 待实现功能

1. **预算推荐**
   - 基于历史支出模式提供个性化预算建议
   - 分类预算分配

2. **数据导入集成**
   - 实现真实数据源替代当前模拟数据
   - 支持从文件导入交易数据

## 团队协作

各模块开发者需要按照以下步骤与分析模块集成：

1. 详见`INTEGRATION_GUIDE.md`文件了解详细集成步骤
2. 分析模块通过`DataAccessInterface`接口与其他模块交互
3. 其他模块需要实现此接口以提供实际数据

## 运行应用

使用提供的批处理脚本启动应用程序：

```
run-simple.bat
```

## 技术栈

- Java 21
- JavaFX 21.0.2 (UI框架)
- Maven (构建工具)
- JFreeChart (图表库) 