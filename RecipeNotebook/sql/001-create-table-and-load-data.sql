DROP TABLE IF EXISTS recipes;

CREATE TABLE recipes (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  image_path VARCHAR(255) DEFAULT '/images/no_image.jpg',
  recipe_source VARCHAR(255),
  servings VARCHAR(255),
  remark VARCHAR(255),
  favorite BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME,
  updated_at DATETIME,
  PRIMARY KEY(id)
);

INSERT INTO recipes (name, image_path, recipe_source, servings, remark, favorite, created_at, updated_at)
VALUES
('卵焼き', '/images/tamagoyaki.jpg', null, '2人前', '基本的な卵焼きのレシピ。', FALSE,'2024-09-22 17:00:00', '2024-10-22 17:00:00'),
('目玉焼き', '/images/medamayaki.jpg', null, '1人前', '基本的な目玉焼きのレシピ。', FALSE, '2024-09-23 17:00:00', '2024-10-23 17:00:00'),
('ナポリタン', '/images/neapolitan.jpg', 'https://www.youtube.com/watch?v=uCbg_uS9aEU&t=452s', '1人前', '料理研究家リュウジさんのレシピをベースに、アレンジを加えたナポリタンのレシピ。ケチャップ減らして醤油とウスターソースを追加して甘さ控えめ・キレのある味わい。', TRUE, '2024-12-01 11:00:00', null),
('ジャージャー風・肉味噌', '/images/jaja_nikumiso.jpg', 'https://www.youtube.com/watch?v=FwoGQ6GlXQs', '7～8食分', '「くまの限界食堂」さんのレシピのうち、肉味噌部分を抜き出してアレンジを加えたレシピ。人参少し足して栄養と甘味追加。にんにくしょうがはみじん切りで炒める。作り置きに最適。', TRUE, '2024-12-01 11:00:00', null);

DROP TABLE IF EXISTS ingredients;

CREATE TABLE ingredients (
  id INT NOT NULL AUTO_INCREMENT,
  recipe_id INT NOT NULL,
  name VARCHAR(255) NOT NULL,
  quantity VARCHAR(255) ,
  arrange BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY(id),
  FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);

INSERT INTO ingredients (recipe_id, name, quantity, arrange)
VALUES
(1, '卵', '3個', FALSE), (1, 'サラダ油', '適量', FALSE), (1, '醤油', '大さじ1/2', FALSE), (1, '砂糖', '大さじ1', FALSE),
(2, '卵', '1個', FALSE), (2, 'サラダ油', '適量', FALSE), (2, '水', NULL, FALSE),
(3, 'パスタ', '100g', FALSE), (3, 'マッシュルーム', '2個', FALSE), (3, 'ウインナー', '3本', FALSE), (3, '玉ねぎ', '50g', FALSE),
(3, 'ピーマン', '1個', FALSE), (3, 'ケチャップ', '大さじ3', TRUE), (3, '醤油', '小さじ1', TRUE), (3, 'ウスターソース', '小さじ1', TRUE),
(3, 'バター', '10g', FALSE), (3, 'オリーブオイル', '適量', TRUE), (3, '塩', '適量', FALSE), (3, '粉チーズ', '適量', FALSE),
(4, '豚ひき肉', '600g', FALSE), (4, 'しいたけ', '6本', FALSE), (4, 'ネギ', '1本', FALSE),(4, '人参', '1/3本', TRUE),
(4, 'Aオイスターソース', '大さじ3', FALSE),(4, 'A味噌', '大さじ3', FALSE),(4, 'A酒', '大さじ3', FALSE),(4, 'A砂糖', '大さじ3', FALSE),
(4, 'しいたけ', '6本', FALSE),(4, 'にんにく', '30g', TRUE),(4, 'しょうが', '30g', TRUE),(4, 'B醤油', '大さじ1', FALSE),
(4, 'Bごま油', '大さじ2~3', FALSE), (4, 'サラダ油', '適量', FALSE);

DROP TABLE IF EXISTS instructions;

CREATE TABLE instructions (
  id INT NOT NULL AUTO_INCREMENT,
  recipe_id INT NOT NULL,
  step_number INT NOT NULL,
  content TEXT NOT NULL,
  arrange BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY(id),
  FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);

INSERT INTO instructions (recipe_id, step_number, content, arrange)
VALUES
(1, 1, '卵を溶いて調味料を混ぜ、卵液を作る', FALSE), (1, 2, 'フライパンに油をたらし、火にかける', FALSE),
(1, 3, '卵液を1/3くらいフライパンに入れて焼き、巻く', TRUE), (1, 4, '3の手順を繰り返して完成', FALSE),
(2, 1, 'フライパンに油をたらし、火にかける', FALSE), (2, 2, 'フライパンに卵を割り入れる', FALSE),
(2, 3, '少し焼けたら水を入れ、ふたをして5分、弱火にかけて完成', FALSE),
(3, 1, '材料をすべてスライスする', FALSE), (3, 2, 'パスタを茹で始める', FALSE), (3, 3, '少量のサラダ油でウインナーを炒める', FALSE),
(3, 4, '玉ねぎをしんなりするまで炒める', FALSE),(3, 5, 'すべての調味料とマッシュルームを入れてしばらく炒める', TRUE),
(3, 6, 'バターとピーマンを入れて少し炒める', FALSE),(3, 7, 'パスタとソースを合わせて焼く', FALSE),(3, 8, '皿に盛ってチーズ振って完成', FALSE),
(4, 1, '長ネギ、しいたけ、にんにく、しょうがをみじん切りにする', FALSE), (4, 2, 'サラダ油で長ネギを香りが出るまで炒めてネギ油を作る', FALSE),
(4, 3, 'にんにく、しょうがを香りが出るまで炒めたら肉を入れる', TRUE),(4, 4, '肉に火が通ったら人参とシイタケを入れて少し炒める', TRUE),
(4, 5, 'Aの調味料を入れて煮詰める', FALSE),(4, 6, 'Bの調味料を入れて1分ほど熱したら完成', FALSE);

DROP TABLE IF EXISTS categories;

CREATE TABLE categories (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  PRIMARY KEY(id)
);

INSERT INTO categories (name)
VALUES ('主菜'), ('副菜'), ('デザート'), ('和食'), ('洋食'), ('ヘルシー');


DROP TABLE IF EXISTS recipe_categories;

CREATE TABLE recipe_categories (
  recipe_id INT NOT NULL,
  category_id INT NOT NULL,
  FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE,
  FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

INSERT INTO recipe_categories (recipe_id, category_id)
VALUES (1, 2), (1, 4), (2, 1);


DROP TABLE IF EXISTS nutrition_facts;

CREATE TABLE nutrition_facts (
  food_category VARCHAR(255) NOT NULL,
  food_number VARCHAR(255) NOT NULL,
  index_number VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL,
  kcal DECIMAL(10,1) NOT NULL,
  protein DECIMAL(10,1),
  fat DECIMAL(10,1),
  carbohydrates DECIMAL(10,1),
  sce DECIMAL(10,1)
);

LOAD DATA INFILE '/var/lib/mysql-files/nutrition_facts.csv'
INTO TABLE nutrition_facts
FIELDS TERMINATED BY ','
OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(food_category, food_number, index_number, name, kcal, protein, fat, carbohydrates, sce);
