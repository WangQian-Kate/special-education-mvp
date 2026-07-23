-- 补全 14 个子行为的专属表现选项
-- B063 使用餐具
INSERT INTO behavior_catalog_option (code, behavior_code, option_type, parent_option_code, label, display_order, requires_custom_text) VALUES
('B063_S01_P01','B063','PERFORMANCE','B063_S01','未正确使用筷子',1,FALSE),('B063_S01_P02','B063','PERFORMANCE','B063_S01','需辅助才正确使用',2,FALSE),('B063_S01_P03','B063','PERFORMANCE','B063_S01','基本能正确使用筷子',3,FALSE),('B063_S01_P04','B063','PERFORMANCE','B063_S01','筷子使用姿势正确',4,FALSE),('B063_S01_P05','B063','PERFORMANCE','B063_S01','其它',5,TRUE),
('B063_S02_P01','B063','PERFORMANCE','B063_S02','未正确使用勺子',1,FALSE),('B063_S02_P02','B063','PERFORMANCE','B063_S02','需辅助才正确使用',2,FALSE),('B063_S02_P03','B063','PERFORMANCE','B063_S02','基本能正确使用勺子',3,FALSE),('B063_S02_P04','B063','PERFORMANCE','B063_S02','勺子使用姿势正确',4,FALSE),('B063_S02_P05','B063','PERFORMANCE','B063_S02','其它',5,TRUE),
('B063_S03_P01','B063','PERFORMANCE','B063_S03','双手不协调',1,FALSE),('B063_S03_P02','B063','PERFORMANCE','B063_S03','需辅助才能协调',2,FALSE),('B063_S03_P03','B063','PERFORMANCE','B063_S03','基本能双手协调吃饭',3,FALSE),('B063_S03_P04','B063','PERFORMANCE','B063_S03','灵活地双手协调吃饭',4,FALSE),('B063_S03_P05','B063','PERFORMANCE','B063_S03','其它',5,TRUE);

-- B077 保持手部清洁
INSERT INTO behavior_catalog_option (code, behavior_code, option_type, parent_option_code, label, display_order, requires_custom_text) VALUES
('B077_S01_P01','B077','PERFORMANCE','B077_S01','脏了未及时洗手',1,FALSE),('B077_S01_P02','B077','PERFORMANCE','B077_S01','需提醒才洗手',2,FALSE),('B077_S01_P03','B077','PERFORMANCE','B077_S01','基本能及时洗手',3,FALSE),('B077_S01_P04','B077','PERFORMANCE','B077_S01','脏了自觉及时洗手',4,FALSE),('B077_S01_P05','B077','PERFORMANCE','B077_S01','其它',5,TRUE),
('B077_S02_P01','B077','PERFORMANCE','B077_S02','脏了未用纸巾擦手',1,FALSE),('B077_S02_P02','B077','PERFORMANCE','B077_S02','需提醒才用纸巾',2,FALSE),('B077_S02_P03','B077','PERFORMANCE','B077_S02','基本能用纸巾擦手',3,FALSE),('B077_S02_P04','B077','PERFORMANCE','B077_S02','脏了自觉用纸巾擦手',4,FALSE),('B077_S02_P05','B077','PERFORMANCE','B077_S02','其它',5,TRUE),
('B077_S03_P01','B077','PERFORMANCE','B077_S03','脏了未用湿纸巾擦手',1,FALSE),('B077_S03_P02','B077','PERFORMANCE','B077_S03','需提醒才用湿纸巾',2,FALSE),('B077_S03_P03','B077','PERFORMANCE','B077_S03','基本能用湿纸巾擦手',3,FALSE),('B077_S03_P04','B077','PERFORMANCE','B077_S03','脏了自觉用湿纸巾擦手',4,FALSE),('B077_S03_P05','B077','PERFORMANCE','B077_S03','其它',5,TRUE);

-- B078 保持衣物整洁
INSERT INTO behavior_catalog_option (code, behavior_code, option_type, parent_option_code, label, display_order, requires_custom_text) VALUES
('B078_S01_P01','B078','PERFORMANCE','B078_S01','脏了未更换',1,FALSE),('B078_S01_P02','B078','PERFORMANCE','B078_S01','需提醒才更换',2,FALSE),('B078_S01_P03','B078','PERFORMANCE','B078_S01','基本能根据情况更换',3,FALSE),('B078_S01_P04','B078','PERFORMANCE','B078_S01','自觉根据情况更换衣物',4,FALSE),('B078_S01_P05','B078','PERFORMANCE','B078_S01','其它',5,TRUE),
('B078_S02_P01','B078','PERFORMANCE','B078_S02','脏了未清洗',1,FALSE),('B078_S02_P02','B078','PERFORMANCE','B078_S02','需提醒才清洗',2,FALSE),('B078_S02_P03','B078','PERFORMANCE','B078_S02','基本能根据情况清洗',3,FALSE),('B078_S02_P04','B078','PERFORMANCE','B078_S02','自觉根据情况清洗衣物',4,FALSE),('B078_S02_P05','B078','PERFORMANCE','B078_S02','其它',5,TRUE);

-- B074 如厕
INSERT INTO behavior_catalog_option (code, behavior_code, option_type, parent_option_code, label, display_order, requires_custom_text) VALUES
('B074_S01_P01','B074','PERFORMANCE','B074_S01','未根据需求小便',1,FALSE),('B074_S01_P02','B074','PERFORMANCE','B074_S01','需提醒才去小便',2,FALSE),('B074_S01_P03','B074','PERFORMANCE','B074_S01','基本能根据需求小便',3,FALSE),('B074_S01_P04','B074','PERFORMANCE','B074_S01','自觉根据生理需求小便',4,FALSE),('B074_S01_P05','B074','PERFORMANCE','B074_S01','其它',5,TRUE),
('B074_S02_P01','B074','PERFORMANCE','B074_S02','未根据需求大便',1,FALSE),('B074_S02_P02','B074','PERFORMANCE','B074_S02','需提醒才去大便',2,FALSE),('B074_S02_P03','B074','PERFORMANCE','B074_S02','基本能根据需求大便',3,FALSE),('B074_S02_P04','B074','PERFORMANCE','B074_S02','自觉根据生理需求大便',4,FALSE),('B074_S02_P05','B074','PERFORMANCE','B074_S02','其它',5,TRUE);

-- B073 清洁地面
INSERT INTO behavior_catalog_option (code, behavior_code, option_type, parent_option_code, label, display_order, requires_custom_text) VALUES
('B073_S01_P01','B073','PERFORMANCE','B073_S01','清洁动作生疏',1,FALSE),('B073_S01_P02','B073','PERFORMANCE','B073_S01','需辅助才完成',2,FALSE),('B073_S01_P03','B073','PERFORMANCE','B073_S01','基本能熟练清理地面',3,FALSE),('B073_S01_P04','B073','PERFORMANCE','B073_S01','熟练流畅清理地面',4,FALSE),('B073_S01_P05','B073','PERFORMANCE','B073_S01','其它',5,TRUE),
('B073_S02_P01','B073','PERFORMANCE','B073_S02','清洁不准确有遗漏',1,FALSE),('B073_S02_P02','B073','PERFORMANCE','B073_S02','需提醒才清理到位',2,FALSE),('B073_S02_P03','B073','PERFORMANCE','B073_S02','基本能准确清理地面',3,FALSE),('B073_S02_P04','B073','PERFORMANCE','B073_S02','准确到位清理地面',4,FALSE),('B073_S02_P05','B073','PERFORMANCE','B073_S02','其它',5,TRUE);

-- B086 课前准备
INSERT INTO behavior_catalog_option (code, behavior_code, option_type, parent_option_code, label, display_order, requires_custom_text) VALUES
('B086_S01_P01','B086','PERFORMANCE','B086_S01','课间未准备书本',1,FALSE),('B086_S01_P02','B086','PERFORMANCE','B086_S01','需提醒才准备',2,FALSE),('B086_S01_P03','B086','PERFORMANCE','B086_S01','基本能课间准备书本',3,FALSE),('B086_S01_P04','B086','PERFORMANCE','B086_S01','自觉课间准备好书本',4,FALSE),('B086_S01_P05','B086','PERFORMANCE','B086_S01','其它',5,TRUE),
('B086_S02_P01','B086','PERFORMANCE','B086_S02','午休后未准备',1,FALSE),('B086_S02_P02','B086','PERFORMANCE','B086_S02','需提醒才准备',2,FALSE),('B086_S02_P03','B086','PERFORMANCE','B086_S02','基本能午休后准备',3,FALSE),('B086_S02_P04','B086','PERFORMANCE','B086_S02','自觉午休后准备用品',4,FALSE),('B086_S02_P05','B086','PERFORMANCE','B086_S02','其它',5,TRUE);
