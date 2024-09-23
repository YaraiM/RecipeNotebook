DROP TABLE IF EXISTS recipes;

CREATE TABLE recipes (
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  image_path VARCHAR(255),
  recipe_source VARCHAR(255),
  servings VARCHAR(255),
  remark VARCHAR(255),
  favorite BOOLEAN NOT NULL DEFAULT FALSE,
  created_at DATETIME,
  updated_at DATETIME,
  PRIMARY KEY(id)
);

INSERT INTO recipes (name, image_path, recipe_source, servings, remark, created_at, updated_at)
VALUES ('卵焼き', 'test1/path', 'https://------1.com', '2人分', '備考欄1', '2024-09-22 17:00:00', '2024-10-22 17:00:00');
INSERT INTO recipes (name, image_path, recipe_source, servings, remark, favorite, created_at, updated_at)
VALUES ('目玉焼き', 'test2/path', 'https://------2.com', '1人分', '備考欄2', TRUE, '2024-09-23 17:00:00', '2024-10-23 17:00:00');


DROP TABLE IF EXISTS ingredients;

CREATE TABLE ingredients (
  id INT NOT NULL AUTO_INCREMENT,
  recipe_id INT NOT NULL,
  name VARCHAR(255) NOT NULL,
  quantity DECIMAL(10, 1),
  unit VARCHAR(255),
  arrange BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY(id),
  FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);

INSERT INTO ingredients (recipe_id, name, quantity, unit, arrange)
VALUES
(1, '卵', 3, '個', FALSE), (1, 'サラダ油', NULL, NULL, FALSE), (1, '醤油', 0.5, '大さじ', FALSE), (1, '砂糖', 1, '大さじ', FALSE),
(2, '卵', 1, '個', FALSE), (2, 'サラダ油', NULL, NULL, FALSE), (2, '水', NULL, NULL, FALSE);


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
(2, 3, '少し焼けたら水を入れ、ふたをして5分、弱火にかけて完成', FALSE);


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
