# Checklist 模板推荐规则

## 目标

模板推荐的目标是在用户开始看房前，基于少量个人场景信息，自动推荐更合适的看房 checklist 和签约 checklist 模块组合。

该功能不替用户做租房决策，只帮助用户少漏检、多关注与自身场景强相关的风险。

## 输入字段

所有字段都应支持跳过。

- `tenantType`：大学生、在职人员、家庭、陪读、病人陪床、其他
- `gender`：女、男、其他、不填写
- `specialPurposes`：陪读、病人陪床、短租过渡、长期稳定居住、远程办公、宠物友好、儿童友好
- `coLivingType`：独居、情侣、室友合租、家庭同住、陪护同住
- `hasPet`：无宠物、有宠物、未来可能养宠物
- `hasChildren`：无孩子、有孩子、陪读或学区相关
- `rentalRegion`：城市、区县、商圈或用户手动输入的区域

## 输出结果

推荐结果由两部分组成：

- `viewingTemplateCodes`：看房阶段启用的模板模块
- `signingTemplateCodes`：签约阶段启用的模板模块
- `recommendationReasons`：推荐原因，用于页面解释和后续调试

用户必须可以在推荐结果上手动增删模块。

## 基础模块

所有用户默认启用以下模块：

- `viewing.basic_property`：房源基础信息、价格、押付、房源一致性
- `viewing.environment`：小区、楼栋、采光、通风、噪音、异味、潮湿
- `viewing.facilities`：水电燃气、网络、家具家电、门窗锁具
- `signing.identity_and_authorization`：身份、产权、授权、可出租性
- `signing.payment_and_deposit`：租金、押金、付款周期、杂费
- `signing.delivery_and_repair`：交割清单、维修责任、房屋交付
- `signing.termination_and_dispute`：退租、违约、押金退还、争议处理

## 推荐规则

### 大学生

- 增加 `viewing.co_living`：合租人数、室友作息、公共区域卫生、费用分摊
- 增加 `viewing.budget_control`：中介费、押金、商水商电、隐藏杂费
- 增加 `signing.co_living_agreement`：合租协议、费用分摊、访客和卫生责任
- 推荐原因：大学生常见风险集中在预算、合租和中介费用。

### 在职人员

- 增加 `viewing.commute_stability`：通勤时长、夜间回家路线、外卖快递便利性
- 增加 `viewing.remote_work`：网络稳定性、手机信号、白天噪音、采光
- 增加 `signing.rent_stability`：续租涨价、租期稳定、房东卖房带看规则
- 推荐原因：在职人员更依赖通勤稳定、网络质量和长期租住确定性。

### 女性独居

- 增加 `viewing.personal_safety`：楼道照明、门禁、监控、门锁、反锁、防盗链
- 增加 `viewing.privacy_risk`：隐藏摄像头、窗帘遮挡、楼间对视、卫生间隐私
- 增加 `signing.entry_rules`：房东进入房屋、带人看房、维修上门提前通知
- 推荐原因：独居场景需要更高优先级处理安全、隐私和进入权限。

### 陪读或有孩子

- 增加 `viewing.child_friendly`：学校距离、楼层、电梯、窗户防护、楼下车流、噪音
- 增加 `viewing.neighborhood_for_children`：周边药店、超市、道路安全、邻里环境
- 增加 `signing.school_and_stability`：租期稳定、居住证或入学材料配合、续租规则
- 推荐原因：陪读和带孩子场景更重视稳定、安全、学校距离和材料配合。

### 病人陪床或老人同住

- 增加 `viewing.medical_access`：医院距离、打车便利、电梯、无障碍、楼层
- 增加 `viewing.health_and_quiet`：通风、采光、噪音、潮湿、霉菌、燃气安全
- 增加 `signing.medical_stability`：租期稳定、提前退租规则、紧急联系人和维修响应
- 推荐原因：陪护场景对通行便利、安静、健康和突发情况处理更敏感。

### 有宠物或未来可能养宠物

- 增加 `viewing.pet_friendly`：房东是否接受宠物、地板材质、异味、隔音、邻里接受度
- 增加 `signing.pet_clause`：宠物押金、清洁费、损坏赔偿、禁止事项
- 推荐原因：宠物相关纠纷通常发生在合同没有提前写清时。

### 所租地区

- 增加 `viewing.local_compliance`：当地群租、隔断、消防、电动车充电、非居住空间出租风险
- 增加 `signing.local_filing`：租赁备案、居住证、城市平台网签、地方资金监管要求
- 推荐原因：不同城市对备案、居住证、群租和资金监管的规则差异较大。

## 隐私约束

- 画像信息默认只保存在本地。
- 性别、陪护、孩子、宠物等字段不应默认进入 AI 分享内容。
- 特殊用途只记录场景标签，不记录病情、学校名称、单位名称等过细个人信息。
- 用户可以删除画像并重新生成模板推荐。

## MVP 实现建议

- 第一版使用规则匹配，不做机器学习推荐。
- 推荐模块以稳定 code 表示，页面展示使用中文标题和推荐原因。
- 用户确认后的结果保存为 `ChecklistTemplateSelection`。
- 后续 checklist item 详情页可以根据模块来源展示“为什么推荐我检查这个”。
