<img src="https://foruda.gitee.com/images/1693470775312764207/27440c57_12260.png" alt="flowlong" width="100px" height="106px">

# 项目介绍

FlowLong🐉飞龙工作流

- 项目说明  `flowlong` 中文名 `飞龙` 在天美好愿景！

> ⭕本项目 `双协议授权` 默认采用 `Apache-2.0` 协议开源 `完全开放` `遵守附加协议` 的前提下允许任何目的商用。
> 如果违反 `附加协议` 自动升级为 `AGPL-3.0` 协议。

> 🔴附加协议：使用方在项目介绍中显著位置`必须标注`版权信息`（链接仓库地址）`，`不允许删除源码注释申明`，`不允许山寨换皮商用与官方企业版竞争`，否则视为侵权`（索赔100万）`。

> 使用必须遵守国家法律法规，⛔不允许非法项目使用，后果自负❗

[企业版💎演示地址](https://aizuda.com)

> 打开官方开发文档 [国外](https://doc.flowlong.com)  [国内](https://flowlong.aizuda.com)

[点击设计器在线演示](https://flowlong-desginer.pages.dev)

[点击设计器源码下载](https://gitee.com/flowlong/flowlong-designer)

# 💎特别用户

<p>
  <a href="http://boot.aizuda.com/?from=mp" target="_blank">
   <img alt="AiZuDa-Logo" src="https://foruda.gitee.com/images/1715955628416785121/954c16ef_12260.png" width="220px" height="80px">
  </a>
</p>

英文字母 `flw` 为 `flowlong workflow` 飞龙工作流的缩写

托管  [GitHub](https://github.com/aizuda/flowlong)  [Gitee](https://gitee.com/aizuda/flowlong)  [GitCode](https://gitcode.com/aizuda/flowlong) 仓库

# 🚩中国特色流程操作概念

| 支持功能 | 功能描述                                                                                    | 完成程度 |
|------|-----------------------------------------------------------------------------------------|------|
| 条件分支 | 排它分支用于在流程中实现决策，即根据条件选择一个分支执行。也用于处理异常情况，将流程路由到特定的异常处理分支。                                 | ✅    |
| 并行分支 | 并行分支允许将流程分成多条分支，也可以把多条分支汇聚到一起。其功能是基于进入和外出顺序流的，即可以分叉`（fork）`成多个并行分支，也可以汇聚`（join）`多个并行分支。 | ✅    |
| 包容分支 | 包容分支可以看做是排它分支和并行分支的结合体。它允许基于条件选择多条分支执行，但如果没有任何一个分支满足条件，则可以选择默认分支。                       | ✅    |
| 路由分支 | 根据条件选择一个分支执行（重定向到指定配置节点），也可以选择默认分支执行（继续往下执行）。                                           | ✅    |
| 父子流程 | 主流程节点设置子流程，子流程节点会自动进入子流程，子流程结束后，主流程节点会自动跳转。（支持同步异步）                                     | ✅    |
| 顺序会签 | 指同一个审批节点设置多个人，如A、B、C三人，三人按顺序依次收到待办，即A先审批，A提交后B才能审批，需全部同意之后，审批才可到下一审批节点。                 | ✅    |
| 并行会签 | 指同一个审批节点设置多个人，如A、B、C三人，三人会同时收到待办任务，需全部同意之后，审批才可到下一审批节点。                                 | ✅    |
| 或签   | 一个流程审批节点里有多个处理人，任意一个人处理后就能进入下一个节点                                                       | ✅    |
| 票签   | 指同一个审批节点设置多个人，如A、B、C三人，分别定义不同的权重，当投票权重比例大于 50% 就能进入下一个节点                                | ✅    |
| 抄送   | 将审批结果通知给抄送列表对应的人，同一个流程实例默认不重复抄送给同一人                                                     | ✅    |
| 驳回   | 将审批重置发送给某节点，重新审批。驳回也叫退回，也可以分退回申请人、退回上一步、任意退回等                                           | ✅    |
| 驳回策略 | 支持驳回策略（1，上一步 2，发起人 3，任意节点），重新审批执行策略（1，继续执行 2，退回驳回节点）                                    | ✅    |
| 分配   | 允许用户自行决定任务转办、委派、主办 及其它                                                                  | ✅    |
| 转办   | A转给其B审批，B审批后，进入下一节点                                                                     | ✅    |
| 离职转办 | A所有参与任务批量转给B审批                                                                          | ✅    |
| 委派   | A转给其B审批，B审批后，转给A，A审批后进入下一节点                                                             | ✅    |
| 代理   | A指定代理人B之后，就不用做任何操作了。B完成任务后，A和B都能查到这个任务，A完成任务，B就看不到任务了                                   | ✅    |
| 跳转   | 可以将当前流程实例跳转到任意办理节点                                                                      | ✅    |
| 拿回   | 在当前办理人尚未处理文件前，允许上一节点提交人员执行拿回                                                            | ✅    |
| 唤醒   | 历史任务唤醒，重新进入审批流程                                                                         | ✅    |
| 撤销   | 流程发起者可以对流程进行撤销处理                                                                        | ✅    |
| 加签   | 允许当前办理人根据需要自行增加当前办理节点的办理人员（前置节点，后置节点）                                                   | ✅    |
| 减签   | 在当前办理人操作之前减少办理人                                                                         | ✅    |
| 追加   | 发起流程动态追加修改节点处理人，更灵活的实例级动态调整节点处理人的可伸缩解决方案                                                | ✅    |
| 认领   | 公共任务认领                                                                                  | ✅    |
| 已阅   | 任务是否查看状态显示                                                                              | ✅    |
| 催办   | 通知当前活动任务处理人办理任务                                                                         | ✅    |
| 沟通   | 与当前活动任务处理人沟通                                                                            | ✅    |
| 终止   | 在任意节点终止流程实例                                                                             | ✅    |
| 定时   | 设置时间节点定时执行任务进入下一步                                                                       | ✅    |
| 触发   | 执行流程触发器业务逻辑实现，结束执行进入下一步，支持【立即触发】【定时触发】两种实现                                              | ✅    |
| 动态构建 | 根据当前任务动态构建执行新任务，并且不体现在流程图中                                                              | ✅    |
| 超时审批 | 根据设置的超时审批时间，超时后自动审批【自动通过或拒绝】                                                            | ✅    |                                                             | ✅    |
| 自动提醒 | 根据设置的提醒时间，提醒审批人审批【可设定提醒次数】实现接口任意方式提醒【短信，邮件，微信，钉钉等】                                      | ✅    |
| 暂存待审 | 流程发起时，可以暂存待审，发起人后续修改，审批重新提交激活流程实例                                                       | ✅    |
| 分组策略 | 角色、部门 分组策略支持认领审批、全部人员参与审批 两种模式                                                          | ✅    |

# 贡献力量

- [运行单元测试](https://gitee.com/aizuda/flowlong/wikis/%E8%BF%90%E8%A1%8C%E5%8D%95%E5%85%83%E6%B5%8B%E8%AF%95)
- PR 请参考现在代码规范注释说明

# 使用文档

- 设计器源码 https://gitee.com/flowlong/flowlong-designer

<img src="https://foruda.gitee.com/images/1683680723972384655/f957e75d_12260.png" alt="flowlong" width="500px" height="262px">

# 其它说明

- 基于 [MybatisPlus](https://baomidou.com) 为 `ORM` 层实现
- 参考了包括 activiti flowable camunda snakerflow 等工作流的设计思想
